// ================================================
// WebMvcConfig.java —— Web MVC 配置
// 作用：注册拦截器、配置跨域等
// 会用在：项目启动时，Spring 加载这些配置
// ================================================

package com.shop.config;

import com.shop.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 *
 * @Configuration 标记这是配置类
 * implements WebMvcConfigurer：重写 Spring MVC 的方法来自定义配置
 *
 * 这里主要做两件事：
 *   1. 注册 JWT 拦截器
 *   2. 配置哪些接口要拦截、哪些放行
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器
     *
     * addPathPatterns：拦截哪些路径
     * excludePathPatterns：放行哪些路径（不需要登录也能访问）
     *
     * 规则说明：
     *   /api/user/register  —— 注册不需要登录
     *   /api/user/login     —— 登录不需要登录
     *   /api/user/info      —— 需要登录（没在 exclude 里）
     *
     *   /api/** 后面的接口（商品、购物车、订单）都需要登录
     *   在购物车和订单模块里，所有接口都需要登录
     *
     * 改进点：实际项目中可以用角色权限控制，这里只做简单的登录校验
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有 /api/ 开头的请求
                .addPathPatterns("/api/**")
                // 但放行注册和登录接口
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login"
                );
    }
}
