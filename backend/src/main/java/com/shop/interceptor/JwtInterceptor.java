// ================================================
// JwtInterceptor.java —— 请求"安检员"
// 作用：检查每一个请求是否带了有效的 Token
// 会用在：需要登录才能访问的接口（如订单、购物车）
// 原理：在请求到达 Controller 之前先审查
// ================================================

package com.shop.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.shop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器 = 请求过滤器的"保安"
 *
 * 执行顺序：
 *   客户端请求 → JwtInterceptor（安检）→ Controller（处理请求）
 *                        ↓
 *                   没通过就返回 401，不让你进去
 *
 * @Component 让 Spring 管理这个拦截器
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * 请求到达 Controller 之前执行
     *
     * @return true = 放行（通过安检），false = 拦截（拒绝访问）
     *
     * 流程：
     *   1. 从请求头取出 Authorization
     *   2. 检查格式是不是 "Bearer xxx"
     *   3. 验证 Token 是否有效
     *   4. 把用户 ID 存到请求属性里，Controller 可以直接取
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // ===== 1. 从请求头取 Token =====
        String authHeader = request.getHeader("Authorization");

        // 没带 Token
        if (authHeader == null || authHeader.isEmpty()) {
            sendUnauthorized(response, "请先登录");
            return false;
        }

        // ===== 2. 检查格式 =====
        // 标准格式：Authorization: Bearer eyJxxx.xxx.xxx
        if (!authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Token 格式错误");
            return false;
        }

        // 截取 "Bearer " 后面的实际 Token
        String token = authHeader.substring(7);

        // ===== 3. 验证 Token =====
        try {
            if (!jwtUtil.validateToken(token)) {
                sendUnauthorized(response, "Token 无效");
                return false;
            }

            // ===== 4. 从 Token 里取出用户 ID =====
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 把 userId 存到 request 属性中
            // Controller 中用 @RequestAttribute("userId") 就能取到
            request.setAttribute("userId", userId);

            // 放行
            return true;

        } catch (TokenExpiredException e) {
            sendUnauthorized(response, "Token 已过期，请重新登录");
            return false;
        } catch (JWTVerificationException e) {
            sendUnauthorized(response, "Token 无效");
            return false;
        }
    }

    /**
     * 给前端返回 401 未授权
     * 内容格式和 Result 保持一致：{ "code": 401, "msg": "xxx", "data": null }
     */
    private void sendUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                "{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}"
        );
    }
}
