// ================================================
// BeanConfig.java —— Spring Bean 配置
// 作用：把一些工具类注册成 Spring Bean，方便 @Autowired 注入
// 会用在：项目启动时自动加载
// ================================================

package com.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Configuration 标记这是一个配置类，Spring 会读取里面的 @Bean
 *
 * 为什么需要这个类？
 * BCryptPasswordEncoder 是 Spring Security 提供的类，
 * 但 Spring 不会自动创建它的实例，需要我们手动注册成 Bean
 */
@Configuration
public class BeanConfig {

    /**
     * 密码加密器
     * 作用：把用户密码加密后再存数据库
     *
     * @Bean 告诉 Spring：这个方法返回的对象要注册成 Bean
     * 方法名 = Bean 的名字，在其他地方可以用 @Autowired 注入
     *
     * BCrypt 特点：
     *   1. 每次加密结果不一样（加了随机盐值）
     *   2. 不可逆（无法从密文反推回明文）
     *   3. 可以调节加密强度（数字越大越安全也越慢）
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
