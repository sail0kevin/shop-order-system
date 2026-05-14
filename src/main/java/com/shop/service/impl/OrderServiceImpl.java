// ================================================
// OrderServiceImpl.java —— 订单业务实现
// 核心：@Transactional 事务管理、库存扣减、超卖防护
// ================================================

package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.Product;
import com.shop.mapper.OrderItemMapper;
import com.shop.mapper.OrderMapper;
import com.shop.mapper.ProductMapper;
import com.shop.service.CartService;
import com.shop.service.OrderService;
import com.shop.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Slf4j  打印日志
 * @Service 业务层
 * @RequiredArgsConstructor 构造方法注入
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;       // 购物车（获取数据 + 清空）
    private final ProductMapper productMapper;   // 商品（查价格、扣库存）
    private final OrderMapper orderMapper;       // 订单表
    private final OrderItemMapper orderItemMapper;
    private final SnowflakeIdUtil snowflakeIdUtil; // 雪花算法ID生成器

    /**
     * 创建订单 —— 核心方法
     *
     * @Transactional 事务注解（关键！）
     * 作用：这个方法里的所有数据库操作，要么全部成功，要么全部失败
     * 如果扣库存成功但插入订单失败 → 库存自动回滚
     *
     * propagation = REQUIRED（默认）：加入当前事务，没有就新建
     * 面试常问：@Transactional 的原理是 AOP 动态代理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId) {
        // ===== 1. 获取购物车数据 =====
        List<CartService.CartItemVO> cartItems = cartService.getCartList(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车是空的");
        }

        // ===== 2. 验证库存并扣减 =====
        // 收集所有商品ID
        List<Long> productIds = cartItems.stream()
                .map(CartService.CartItemVO::getProductId)
                .collect(Collectors.toList());

        // 从 MySQL 查最新的商品信息（价格、库存）
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 逐件商品验证 + 扣库存
        for (CartService.CartItemVO item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new RuntimeException("商品 " + item.getProductId() + " 不存在");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品 " + product.getName() + " 库存不足");
            }

            // 扣减库存 —— 关键 SQL！
            // UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?
            // WHERE stock >= ? 这个条件防止超卖
            // 如果两条一模一样的请求同时到达：
            //   MySQL 的行锁会让第二条等待
            //   第一条执行完，stock 变少了
            //   第二条发现 stock >= quantity 不成立，影响行数为 0
            int affected = productMapper.update(null, new UpdateWrapper<Product>()
                    .eq("id", item.getProductId())
                    .ge("stock", item.getQuantity())
                    .setSql("stock = stock - " + item.getQuantity()));

            if (affected == 0) {
                throw new RuntimeException("商品 " + product.getName() + " 库存不足");
            }
        }

        // ===== 3. 计算总金额 =====
        BigDecimal totalAmount = cartItems.stream()
                .map(CartService.CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ===== 4. 生成订单编号 =====
        String orderNo = generateOrderNo();

        // ===== 5. 保存订单 =====
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);  // 待支付
        orderMapper.insert(order);

        // ===== 6. 保存订单明细 =====
        for (CartService.CartItemVO item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(item.getSubtotal());
            orderItemMapper.insert(orderItem);
        }

        // ===== 7. 清空购物车 =====
        cartService.clearCart(userId);

        log.info("下单成功: orderNo={}, userId={}, totalAmount={}", orderNo, userId, totalAmount);

        return order;
    }

    /**
     * 支付订单
     *
     * 模拟支付：将待支付订单（status=0）更新为已支付（status=1）
     * 会校验：
     *   1. 订单是否存在
     *   2. 订单是否属于当前用户（防越权）
     *   3. 订单状态是否为待支付（防重复支付）
     */
    @Override
    public Order payOrder(String orderNo, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态错误，无法支付");
        }

        // 更新状态为已支付
        order.setStatus(1);
        orderMapper.updateById(order);

        log.info("支付成功: orderNo={}, userId={}", orderNo, userId);
        return order;
    }

    /**
     * 生成订单编号
     * 使用雪花算法：全局唯一、趋势递增、高性能
     * 相比原来的 Random 方案，不会重复，且能根据 ID 反推时间
     */
    private String generateOrderNo() {
        return String.valueOf(snowflakeIdUtil.nextId());
    }

    /**
     * 用户订单列表
     */
    @Override
    public Page<Order> getUserOrders(Long userId, int page, int size) {
        Page<Order> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(pageObj, wrapper);
    }
}
