// ================================================
// ProductService.java —— 商品业务接口
// 作用：定义商品相关的查询方法
// ================================================

package com.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.entity.Product;

public interface ProductService {

    /**
     * 分页查询商品列表
     *
     * @param page     页码（从1开始）
     * @param size     每页条数
     * @param category 分类（可选，传 null 查全部）
     * @param keyword  关键词（可选，按名称模糊搜索）
     * @return 分页结果（包含当前页数据 + 总条数）
     */
    Page<Product> getProductPage(int page, int size, String category, String keyword);

    /**
     * 查询商品详情（带 Redis 缓存）
     *
     * @param id 商品ID
     * @return 商品对象
     *
     * 缓存策略：
     *   1. 先查 Redis，有就直接返回（缓存命中）
     *   2. Redis 没有就查数据库
     *   3. 查到的结果存到 Redis，下次直接从缓存取
     *   4. 缓存过期时间：1小时
     */
    Product getProductDetail(Long id);
}
