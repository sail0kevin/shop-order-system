// ================================================
// ProductController.java —— 商品接口
// 作用：提供商品列表和详情的查询接口
// 会用在：前端商城页面展示商品
// ================================================

package com.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.Result;
import com.shop.entity.Product;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 *
 * 所有接口都是 GET（只查不改，后续 Day3 购物车和订单才需要 POST）
 * 所有商品接口都需登录（由拦截器检验 Token）
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 商品列表（分页 + 筛选 + 搜索）
     *
     * 请求方式：GET
     * 请求地址：/api/product/list?page=1&size=10&category=电子产品&keyword=iPhone
     *
     * 参数说明：
     *   page     — 页码，默认第1页
     *   size     — 每页条数，默认10
     *   category — 分类筛选（可选），比如传 "电子产品" 只查这个分类
     *   keyword  — 关键词搜索（可选），按商品名称模糊匹配
     *
     * 返回示例：
     *   {
     *     "code": 200,
     *     "data": {
     *       "records": [ ...商品列表... ],
     *       "total": 100,          // 总条数
     *       "current": 1,          // 当前页码
     *       "pages": 10            // 总页数
     *     }
     *   }
     */
    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        Page<Product> result = productService.getProductPage(page, size, category, keyword);
        return Result.success(result);
    }

    /**
     * 商品详情
     *
     * 请求方式：GET
     * 请求地址：/api/product/detail/1
     *
     * 路径变量 @PathVariable：从 URL 中取出 {id}
     * 比如 /api/product/detail/3 → id = 3
     *
     * 这个接口会用到 Redis 缓存：
     *   第一次查 → 查数据库 → 存 Redis
     *   第二次查 → 直接返回 Redis（不用查数据库，快很多）
     */
    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }
}
