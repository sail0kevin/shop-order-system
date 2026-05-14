// ================================================
// MyBatisPlusConfig.java —— MyBatis-Plus 插件配置
// 作用：注册分页插件（不配这个，分页查询会失效）
// 会用在：ProductService 里的 selectPage 方法
// ================================================

package com.shop.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 拦截器配置
 *
 * MybatisPlusInterceptor：MyBatis-Plus 的插件拦截器
 * PaginationInnerInterceptor：分页拦截器，自动拦截分页查询
 *   原理：拦截 selectPage 方法，自动在 SQL 后面拼接 LIMIT ?, ?
 *   比如：SELECT * FROM product → SELECT * FROM product LIMIT 0, 10
 *
 * DbType.MYSQL：告诉分页插件用 MySQL 的语法（不同数据库分页语法不一样）
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
