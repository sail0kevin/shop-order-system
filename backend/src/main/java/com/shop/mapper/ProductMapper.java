// ================================================
// ProductMapper.java —— 商品数据库操作层
// 继承 BaseMapper<Product>，自带单表 CRUD
// 会用在：ProductService 里调用
// ================================================

package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.Product;

/**
 * MyBatis-Plus 的 BaseMapper 已经提供了以下方法：
 *   - selectPage(Page, Wrapper)    —— 分页查询（Day2 核心功能）
 *   - selectById(id)               —— 查详情
 *   - selectList(Wrapper)          —— 条件查询
 *   - insert / updateById / deleteById
 *
 * 分页查询用法：
 *   Page<Product> page = new Page<>(1, 10);  // 第1页，每页10条
 *   LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
 *   wrapper.eq(Product::getCategory, "电子产品");
 *   productMapper.selectPage(page, wrapper);
 *   // page.getRecords() 获取当前页数据
 *   // page.getTotal()   获取总条数
 */
public interface ProductMapper extends BaseMapper<Product> {

}
