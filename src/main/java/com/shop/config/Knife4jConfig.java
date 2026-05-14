// ================================================
// Knife4jConfig.java —— 接口文档配置
// 作用：自动生成 API 文档，方便调试接口
// 会用在：开发环境调试，浏览器打开 http://localhost:8080/doc.html
// 提示：上线前应该关闭 Knife4j，防止接口信息泄露
// ================================================

package com.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

/**
 * Knife4j 配置（基于 Swagger 2）
 *
 * 配置什么？
 *   - 文档标题、描述
 *   - 扫描哪个包下的 Controller
 *   - 全局 Token 认证（这样在文档里可以统一传 Token）
 */
@Configuration  // 标记这是配置类
public class Knife4jConfig {

    /**
     * 配置 Docket（Swagger 的核心配置类）
     *
     * @Bean 把这个配置注册到 Spring 中
     * select() 开始配置 → apis() 指定扫描包 → paths() 指定路径 → build() 完成
     *
     * 改进点：可以用 @Profile("dev") 限制只在开发环境生效
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())                              // API 文档基本信息
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.shop.controller"))  // 扫描 controller 包
                .paths(PathSelectors.any())                      // 所有路径都生成文档
                .build()
                .securitySchemes(securitySchemes())              // 配置全局 Token
                .securityContexts(securityContexts());
    }

    /**
     * API 文档基本信息
     * 会显示在 Swagger 页面的顶部
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("商城订单系统 API 文档")
                .description("包含用户、商品、购物车、订单模块")
                .version("1.0.0")
                .build();
    }

    /**
     * 配置全局 Token 认证
     * 在 Swagger 页面点击 "Authorize" 按钮，输入 Token
     * 之后每个请求都会自动带上 Authorization: Bearer xxx
     *
     * 这样调试需要登录的接口时，不用手动在每个请求里加 Token
     */
    private List<ApiKey> securitySchemes() {
        return Collections.singletonList(
                new ApiKey("Authorization", "Authorization", "header")
        );
    }

    /**
     * Token 作用域配置
     */
    private List<SecurityContext> securityContexts() {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(Collections.singletonList(
                                new SecurityReference("Authorization",
                                        new AuthorizationScope[]{
                                                new AuthorizationScope("global", "全局")
                                        })
                        ))
                        .build()
        );
    }
}
