// ================================================
// CartServiceImpl.java —— 购物车业务实现
// 所有数据存在 Redis Hash 中
// 结构：cart:{userId} → { productId: quantity }
// ================================================

package com.shop.service.impl;

import com.shop.entity.Product;
import com.shop.mapper.ProductMapper;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private final ProductMapper productMapper;

    /** Redis key 前缀 */
    private static final String CART_KEY_PREFIX = "cart:";

    /** 购物车 Redis 的完整 key */
    private String getCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    @Override
    public void addCart(Long userId, Long productId, Integer quantity) {
        String key = getCartKey(userId);
        // opsForHash() → 操作 Redis Hash 结构
        // increment: 如果 field 不存在，设成 quantity；如果存在，累加
        redisTemplate.opsForHash().increment(key, String.valueOf(productId), quantity);
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String key = getCartKey(userId);
        // 直接覆盖：把 field 的值设成新数量
        redisTemplate.opsForHash().put(key, String.valueOf(productId), String.valueOf(quantity));
    }

    @Override
    public void removeCartItem(Long userId, Long productId) {
        String key = getCartKey(userId);
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));
    }

    @Override
    public List<CartItemVO> getCartList(Long userId) {
        // 1. 从 Redis 取全部购物车数据
        String key = getCartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 收集所有商品ID，去 MySQL 查详细信息
        List<Long> productIds = entries.keySet().stream()
                .map(k -> Long.valueOf(k.toString()))
                .collect(Collectors.toList());

        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3. 拼装返回结果
        List<CartItemVO> cartItems = new ArrayList<>();
        entries.forEach((productIdObj, quantityObj) -> {
            Long pid = Long.valueOf(productIdObj.toString());
            Integer qty = Integer.valueOf(quantityObj.toString());
            Product product = productMap.get(pid);

            if (product != null) {
                cartItems.add(new CartItemVO(
                        pid,
                        product.getName(),
                        product.getImage(),
                        product.getPrice(),
                        qty
                ));
            }
        });

        return cartItems;
    }

    @Override
    public void clearCart(Long userId) {
        String key = getCartKey(userId);
        redisTemplate.delete(key);
    }
}
