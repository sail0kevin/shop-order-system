// ================================================
// ShopApplication.java —— 项目的"开关"
// 作用：启动整个 Spring Boot 项目
// 会用在：运行 main 方法启动服务器
// 位置：com.shop 包下，Spring Boot 会自动扫描子包
// ================================================

package com.shop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication 是 Spring Boot 的核心注解
// 它组合了三个注解的功能：
//   1. @Configuration —— 允许这个类定义 Bean
//   2. @EnableAutoConfiguration —— Spring Boot 自动配置
//   3. @ComponentScan —— 自动扫描 com.shop 包下的所有组件
//
// @MapperScan("com.shop.mapper") —— 扫描 Mapper 接口
// MyBatis-Plus 需要知道 Mapper 接口在哪里，才能生成代理对象
// 不加这个注解，UserMapper 注入时会报错
@SpringBootApplication
@MapperScan("com.shop.mapper")
public class ShopApplication {

    /**
     * 项目入口，直接运行这个方法就可以启动整个项目
     * 启动后在浏览器访问 http://localhost:8080
     * Swagger 文档地址：http://localhost:8080/doc.html
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
