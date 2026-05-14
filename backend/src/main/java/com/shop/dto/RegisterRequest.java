// ================================================
// RegisterRequest.java —— 注册请求参数
// 作用：封装前端传过来的注册数据
// 会用在：UserController 的注册接口
// ================================================

package com.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 注册请求 DTO
 * 比登录多了昵称字段，而且密码有长度限制
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度 6-32 个字符")
    private String password;

    private String nickname;  // 昵称（选填）
}
