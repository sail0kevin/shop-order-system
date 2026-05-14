// ================================================
// LoginRequest.java —— 登录请求参数
// 作用：封装前端传过来的登录数据
// 会用在：UserController 的登录接口
// ================================================

package com.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 * DTO = Data Transfer Object —— 只管传数据，不涉及数据库
 *
 * @NotBlank 注解：验证用户名和密码不能为空
 * 如果为空，GlobalExceptionHandler 会捕获并返回错误信息
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
