// ================================================
// Result.java —— 统一的"快递盒"
// 作用：所有接口返回的数据都装在这个盒子里
// 格式：{ "code": 200, "msg": "成功", "data": { ... } }
// 会用在：每个 Controller 的返回值
// 好处：前端统一处理成功/失败，不用每个接口单独判断
// ================================================

package com.shop.common;

import lombok.Data;

/**
 * 统一响应结果类
 * @param <T> data 的类型，比如 User、List<Product>、String 等
 *
 * 用法示例：
 *   // 成功，返回数据
 *   return Result.success(user);
 *   // 成功，不返回数据
 *   return Result.success();
 *   // 失败，带错误信息
 *   return Result.error("用户名已存在");
 */
@Data  // Lombok 注解：自动生成 getter/setter/toString
public class Result<T> {

    /**
     * 状态码
     * 200  — 成功
     * 400  — 参数错误
     * 401  — 未登录/Token 失效
     * 500  — 服务器内部错误
     */
    private int code;

    private String msg;  // 提示信息，如"登录成功"、"用户名或密码错误"
    private T data;      // 真实返回的数据，泛型 T 可以是任何类型

    // ========== 构造方法（private，不允许外面直接 new） ==========

    private Result() {}

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ========== 静态工厂方法（推荐用这些方法创建 Result） ==========

    /**
     * 操作成功，返回数据
     * 比如：查询用户信息成功，把用户对象传进来
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "成功", data);
    }

    /**
     * 操作成功，不返回数据
     * 比如：删除用户成功，不需要返回内容
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "成功", null);
    }

    /**
     * 操作失败，自定义错误信息
     * 比如：密码错误、用户名已存在
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(400, msg, null);
    }

    /**
     * 未登录 / 认证失败
     * 比如：Token 过期、没带 Token
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null);
    }

    /**
     * 服务器内部错误
     * 比如：空指针异常、数据库连接失败
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "服务器内部错误", null);
    }
}
