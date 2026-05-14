// ================================================
// OrderService.java —— 订单业务接口
// ================================================

package com.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.entity.Order;

public interface OrderService {

    /**
     * 从购物车创建订单
     *
     * 流程：
     *   1. 查 Redis 购物车数据
     *   2. 查 MySQL 商品信息（价格、库存）
     *   3. 验证库存是否充足
     *   4. 扣减库存（UPDATE ... WHERE stock >= ?，防超卖）
     *   5. 生成订单编号
     *   6. 插入订单表 + 订单明细表
     *   7. 清空 Redis 购物车
     *
     * 整个流程在 @Transactional 事务中执行
     * 任何一步失败 → 全部回滚（库存不扣、订单不创建）
     *
     * @param userId 用户ID
     * @return 生成的订单对象
     */
    Order createOrder(Long userId);

    /**
     * 支付订单
     *
     * 将订单状态从 0（待支付）更新为 1（已支付）
     * 校验订单归属和状态，防止重复支付
     *
     * @param orderNo 订单编号
     * @param userId  用户ID
     * @return 更新后的订单对象
     */
    Order payOrder(String orderNo, Long userId);

    /**
     * 查询用户订单列表（分页）
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页数据
     */
    Page<Order> getUserOrders(Long userId, int page, int size);
}
