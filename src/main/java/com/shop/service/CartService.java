// ================================================
// CartService.java —— 购物车业务接口
// 作用：购物车的增删改查，全部操作 Redis
// 不会用在：MySQL（购物车数据不存数据库）
// ================================================

package com.shop.service;

import com.shop.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     * 如果商品已在购物车中，累加数量
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @param quantity  数量
     */
    void addCart(Long userId, Long productId, Integer quantity);

    /**
     * 修改购物车中某个商品的数量
     *
     * @param userId   用户ID
     * @param productId 商品ID
     * @param quantity  新数量
     */
    void updateQuantity(Long userId, Long productId, Integer quantity);

    /**
     * 从购物车中删除某个商品
     *
     * @param userId   用户ID
     * @param productId 商品ID
     */
    void removeCartItem(Long userId, Long productId);

    /**
     * 查看购物车列表
     * 返回结果是 Redis 里的商品ID和数量，
     * 以及商品的详细信息（名称、价格、图片）
     *
     * @param userId 用户ID
     * @return 购物车条目列表（每个条目包含商品详情 + 数量 + 小计）
     */
    List<CartItemVO> getCartList(Long userId);

    /**
     * 清空购物车
     * 下单成功后调用
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 购物车条目视图对象
     * 包含商品信息和数量，返回给前端展示
     */
    @lombok.Data
    class CartItemVO {
        private Long productId;       // 商品ID
        private String productName;   // 商品名称
        private String productImage;  // 商品图片
        private BigDecimal price;      // 单价
        private Integer quantity;     // 数量
        private BigDecimal subtotal;  // 小计 = 单价 × 数量

        public CartItemVO(Long productId, String productName, String productImage,
                         BigDecimal price, Integer quantity) {
            this.productId = productId;
            this.productName = productName;
            this.productImage = productImage;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
