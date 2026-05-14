// ================================================
// GlobalExceptionHandler.java —— 项目的"安全网"
// 作用：拦截所有 Controller 没处理的异常，统一返回
// 会用在：任何地方抛出异常时，由这里统一处理
// 好处：不会把异常的堆栈信息直接暴露给前端
// ================================================

package com.shop.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 作用：监听所有 Controller，捕获抛出的异常
 *
 * 执行流程：
 *   Controller 报错 → 找 @ExceptionHandler 匹配的方法 → 返回 Result
 */
@Slf4j  // Lombok 注解：自动生成 log 对象，用来打印日志
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理"参数校验失败"异常
     * 比如：用户名不能为空、密码长度不够
     * 会用在：Controller 参数加了 @Valid 注解时
     *
     * 改进点：可以在这里区分字段名，返回具体哪个字段错了
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public Result<Void> handleBindException(org.springframework.validation.BindException e) {
        // 拿到第一个校验失败的错误信息
        String msg = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(msg);
    }

    /**
     * 处理"业务异常"（自定义异常，后面会用到）
     * 比如：商品库存不足、订单状态错误
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 处理"兜底异常"——上面没匹配到的所有异常都走这里
     * 比如：空指针、数组越界
     * log.error 会打印堆栈信息，方便排查问题
     *
     * 改进点：生产环境不应该把详细错误信息返回给前端
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 打印错误日志（只看这一条就能定位问题）
        log.error("系统异常: ", e);
        return Result.error();
    }
}
