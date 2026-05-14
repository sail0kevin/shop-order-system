// ================================================
// CartController.java —— 购物车接口
// 购物车数据存 Redis，通过 @RequestAttribute 获取用户ID
// ================================================

package com.shop.controller;

import com.shop.common.Result;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 添加商品到购物车
     * POST /api/cart/add
     *
     * 请求体：{ "productId": 1, "quantity": 2 }
     * 说明：如果商品已在购物车，数量会累加
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestAttribute("userId") Long userId,
                            @RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        cartService.addCart(userId, productId, quantity);
        return Result.success();
    }

    /**
     * 修改购物车商品数量
     * POST /api/cart/update
     *
     * 请求体：{ "productId": 1, "quantity": 5 }
     * 说明：直接覆盖数量
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestAttribute("userId") Long userId,
                               @RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        cartService.updateQuantity(userId, productId, quantity);
        return Result.success();
    }

    /**
     * 删除购物车中的某个商品
     * POST /api/cart/remove
     *
     * 请求体：{ "productId": 1 }
     */
    @PostMapping("/remove")
    public Result<Void> remove(@RequestAttribute("userId") Long userId,
                               @RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        cartService.removeCartItem(userId, productId);
        return Result.success();
    }

    /**
     * 查看购物车列表
     * GET /api/cart/list
     *
     * 返回购物车中所有商品，包含商品信息和数量
     */
    @GetMapping("/list")
    public Result<List<CartService.CartItemVO>> list(@RequestAttribute("userId") Long userId) {
        List<CartService.CartItemVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }
}
