// ================================================
// UserService.java —— 用户业务层（接口）
// 作用：定义用户相关的业务逻辑
//       接口的作用是定义"做什么"，实现类负责"怎么做"
// 会用在：UserController 调用这些方法
// 好处：面向接口编程，以后可以切换实现类不改 Controller
// ================================================

package com.shop.service;

import com.shop.dto.LoginRequest;
import com.shop.dto.RegisterRequest;
import com.shop.entity.User;

/**
 * 用户服务接口
 *
 * 业务层的作用：
 *   1. 处理复杂的业务逻辑（不只是增删改查）
 *   2. 调用 Mapper 操作数据库
 *   3. 处理异常情况（用户名重复、密码错误等）
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册信息（用户名、密码、昵称）
     * @throws RuntimeException 如果用户名已存在
     *
     * 流程：检查用户名是否已存在 → 加密密码 → 存入数据库
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录信息（用户名、密码）
     * @return JWT Token（登录成功后生成的令牌）
     * @throws RuntimeException 如果用户名或密码错误
     *
     * 流程：查用户 → 比对密码 → 生成 Token → 返回
     */
    String login(LoginRequest request);

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户对象（不含密码）
     *
     * 会用在：拦截器验证 Token 后获取当前用户信息
     */
    User getUserById(Long id);
}
