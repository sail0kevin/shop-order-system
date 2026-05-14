// ================================================
// UserController.java —— 用户接口（入口层）
// 作用：接收 HTTP 请求，调用 Service，返回结果
// 会用在：前端调用注册/登录接口
// ================================================

package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.LoginRequest;
import com.shop.dto.RegisterRequest;
import com.shop.entity.User;
import com.shop.service.UserService;
import com.shop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * @RestController = @Controller + @ResponseBody
 *   每个方法返回值直接变成 JSON 返回给前端
 *
 * @RequestMapping("/api/user")
 *   这个控制器所有接口都以 /api/user 开头
 *   比如注册接口就是 POST /api/user/register
 *
 * @RequiredArgsConstructor
 *   给 final 字段生成构造方法，Spring 自动注入
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * 请求方式：POST
     * 请求地址：/api/user/register
     * 请求体（JSON）：
     *   {
     *     "username": "kevin",      // 必填
     *     "password": "123456",     // 必填
     *     "nickname": "凯文"        // 选填
     *   }
     * 返回结果：
     *   { "code": 200, "msg": "成功", "data": null }
     *
     * @Valid 注解：校验请求参数（用户名不能为空、密码不能太短等）
     * 校验失败时，GlobalExceptionHandler 会处理
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    /**
     * 用户登录
     *
     * 请求方式：POST
     * 请求地址：/api/user/login
     * 请求体（JSON）：
     *   {
     *     "username": "kevin",
     *     "password": "123456"
     *   }
     * 返回结果：
     *   {
     *     "code": 200,
     *     "msg": "成功",
     *     "data": {
     *       "token": "eyJxxx.xxx.xxx",      // JWT Token
     *       "userId": 1                      // 用户 ID
     *     }
     *   }
     *
     * 前端拿到 Token 后存在 localStorage，
     * 后续请求在 Header 里带上 Authorization: Bearer xxx
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("refreshToken", refreshToken);
        return Result.success(data);
    }

    /**
     * 刷新 Token
     *
     * POST /api/user/refresh
     * 请求体：{ "refreshToken": "xxx" }
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        String oldRefreshToken = body.get("refreshToken");
        if (oldRefreshToken == null) {
            return Result.error("refreshToken 不能为空");
        }
        String newToken = jwtUtil.refreshAccessToken(oldRefreshToken);
        String newRefreshToken = jwtUtil.generateRefreshToken(
                jwtUtil.getUserIdFromToken(oldRefreshToken));

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        data.put("refreshToken", newRefreshToken);
        return Result.success(data);
    }

    /**
     * 获取当前登录用户信息
     *
     * 请求方式：GET
     * 请求地址：/api/user/info
     * 请求头：Authorization: Bearer xxx（从登录接口获取的 Token）
     * 返回结果：{ "code": 200, "data": { "id": 1, "username": "kevin", ... } }
     *
     * @RequestAttribute：从 JwtInterceptor 里取出来的用户ID
     * JwtInterceptor 会在请求到达之前把用户ID放进去
     *
     * 改进点：实际项目中应该返回专门的 VO（不含敏感字段），而不是 Entity
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute("userId") Long userId) {
        User user = userService.getUserById(userId);
        return Result.success(user);
    }
}
