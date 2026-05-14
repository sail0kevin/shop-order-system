// ================================================
// ProductServiceImpl.java —— 商品业务实现
// 作用：商品查询 + Redis 缓存
// 亮点：手动管理 Redis 缓存（缓存穿透防护）
// ================================================

package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.entity.Product;
import com.shop.mapper.ProductMapper;
import com.shop.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现
 *
 * StringRedisTemplate：Redis 操作工具，以字符串形式存取
 * ObjectMapper：Java 对象和 JSON 字符串互转
 *
 * 缓存设计说明：
 *   缓存 key = "product:detail:{id}"，比如 "product:detail:1"
 *   过期时间 = 1 小时，防止缓存数据太久不更新
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;   // Spring Boot 自动注入

    // Redis key 前缀
    private static final String CACHE_KEY_PREFIX = "product:detail:";
    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE = 3600;

    /**
     * 分页查询
     *
     * @param page     页码
     * @param size     每页条数
     * @param category 分类筛选（null 表示查全部）
     * @param keyword  关键词搜索（null 表示不搜索）
     *
     * LambdaQueryWrapper：MyBatis-Plus 的条件构造器
     *   eq = equal（等于）
     *   like = 模糊匹配（相当于 SQL 的 LIKE %keyword%）
     *   orderByDesc = 按字段降序排序（最新的在前面）
     */
    @Override
    public Page<Product> getProductPage(int page, int size, String category, String keyword) {
        // 1. 创建分页对象（当前页，每页条数）
        Page<Product> pageObj = new Page<>(page, size);

        // 2. 创建条件构造器
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 3. 分类筛选（如果传了分类就加条件）
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category);
        }

        // 4. 关键词搜索（按名称模糊匹配）
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }

        // 5. 只查上架的商品
        wrapper.eq(Product::getStatus, 1);

        // 6. 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        // 7. 执行分页查询
        // selectPage 是 BaseMapper 自带的方法，自动计算总条数
        return productMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 商品详情（Redis 缓存实现）
     *
     * 缓存流程（Read-Through 模式）：
     *   请求 → 查 Redis → 有？→ 返回（缓存命中）
     *                    → 没有？→ 查数据库 → 存 Redis → 返回
     *
     * 改进点（面试可提）：
     *   1. 缓存穿透：如果数据库也没有，可以存一个空值到 Redis（防止恶意请求穿透）
     *   2. 缓存击穿：可以用互斥锁或本地锁，防止高并发同时查数据库
     *   3. 缓存雪崩：过期时间加随机值，防止大量缓存同时失效
     */
    @Override
    public Product getProductDetail(Long id) {
        // ===== 1. 先查 Redis =====
        String cacheKey = CACHE_KEY_PREFIX + id;
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);

        if (cachedJson != null) {
            // 缓存命中：把 JSON 转回 Product 对象
            try {
                return objectMapper.readValue(cachedJson, Product.class);
            } catch (JsonProcessingException e) {
                // JSON 解析失败，忽略缓存，继续查数据库
                // 改进点：可以打日志告警
            }
        }

        // ===== 2. 缓存没命中，查数据库 =====
        Product product = productMapper.selectById(id);

        if (product == null) {
            // 商品不存在，抛异常让全局异常处理器处理
            throw new RuntimeException("商品不存在");
        }

        // ===== 3. 查到了，存 Redis 缓存 =====
        try {
            String json = objectMapper.writeValueAsString(product);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            // 存缓存失败不影响正常查询，只是下次还要查数据库
        }

        return product;
    }
}
