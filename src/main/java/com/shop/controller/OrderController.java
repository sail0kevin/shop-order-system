// ================================================
// OrderController.java —— 订单接口
// 核心：下单（事务）、订单列表
// ================================================

package com.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.Result;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单（从购物车下单）
     *
     * POST /api/order/create
     * 请求头：Authorization: Bearer xxx
     * 请求体：不需要传参数，从购物车取数据
     *
     * 流程：
     *   购物车有商品 → 点下单 → 扣库存 → 生成订单 → 清空购物车
     */
    @PostMapping("/create")
    public Result<Order> create(@RequestAttribute("userId") Long userId) {
        Order order = orderService.createOrder(userId);
        return Result.success(order);
    }

    /**
     * 支付订单（模拟）
     *
     * POST /api/order/pay
     * 请求头：Authorization: Bearer xxx
     * 请求体：{"orderNo": "订单编号"}
     *
     * 说明：模拟支付，不接真实支付通道
     * 将订单状态从 待支付(0) → 已支付(1)
     */
    @PostMapping("/pay")
    public Result<Order> pay(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> body) {
        String orderNo = body.get("orderNo");
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("订单编号不能为空");
        }
        Order order = orderService.payOrder(orderNo, userId);
        return Result.success(order);
    }

    /**
     * 订单列表（分页）
     *
     * GET /api/order/list?page=1&size=10
     */
    @GetMapping("/list")
    public Result<Page<Order>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> result = orderService.getUserOrders(userId, page, size);
        return Result.success(result);
    }
}
