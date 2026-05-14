// ================================================
// JwtUtil.java —— JWT 工具类
// 作用：生成 Token（登录成功时用）
//       验证 Token（拦截器每次请求时用）
// 会用在：UserController（登录）、JwtInterceptor（验证）
// 原理：JWT = Header.Payload.Signature（三段式）
// ================================================

package com.shop.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * JWT 工具类
 *
 * @Component 让 Spring 管理这个类，可以在别的地方用 @Autowired 注入
 */
@Component
public class JwtUtil {

    /**
     * 密钥：从 application.yml 读取 shop.jwt.secret
     * 这个密钥很重要——用它来签名和验证 Token
     * 如果泄露，别人就可以伪造 Token 冒充任何用户
     */
    @Value("${shop.jwt.secret}")
    private String secret;

    /**
     * 过期时间：从 application.yml 读取 shop.jwt.expire
     */
    @Value("${shop.jwt.expire}")
    private long expire;

    /**
     * 生成 Token（登录成功时调用）
     *
     * @param userId 用户ID，存在 Token 里，后面可以从 Token 中取出
     * @return JWT 字符串
     */
    public String generateToken(Long userId) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        long now = System.currentTimeMillis();
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("user_id", userId);
        builder.withClaim("type", "access");
        builder.withIssuedAt(new Date(now));
        builder.withExpiresAt(new Date(now + expire));
        return builder.sign(algorithm);
    }

    /**
     * 生成刷新 Token（有效期 7 天）
     * 当 access_token 过期时，用 refresh_token 换取新的 access_token
     */
    public String generateRefreshToken(Long userId) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        long now = System.currentTimeMillis();
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("user_id", userId);
        builder.withClaim("type", "refresh");
        builder.withIssuedAt(new Date(now));
        builder.withExpiresAt(new Date(now + 604800000L)); // 7天
        return builder.sign(algorithm);
    }

    /**
     * 刷新 Token：验证 refresh_token 有效性，返回新的 access_token
     */
    public String refreshAccessToken(String refreshToken) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(refreshToken);

        String type = jwt.getClaim("type").asString();
        if (!"refresh".equals(type)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        Long userId = jwt.getClaim("user_id").asLong();
        return generateToken(userId);
    }

    /**
     * 从 Token 中取出用户ID
     *
     * @param token JWT 字符串
     * @return 用户ID（存在 Token 里的）
     * @throws JWTVerificationException 如果 Token 无效或过期
     */
    public Long getUserIdFromToken(String token) {
        // 1. 用同样的密钥创建验证器
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();

        // 2. 验证并解码 Token
        DecodedJWT jwt = verifier.verify(token);

        // 3. 取出之前存进去的 user_id
        return jwt.getClaim("user_id").asLong();
    }

    /**
     * 验证 Token 是否有效（JwtInterceptor 会调用）
     *
     * @param token JWT 字符串
     * @return true=有效，false=无效或过期
     *
     * 改进点：这里只是简单的 true/false
     * 如果需要更详细的错误原因（过期/无效/被篡改），可以改成返回 String
     */
    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
