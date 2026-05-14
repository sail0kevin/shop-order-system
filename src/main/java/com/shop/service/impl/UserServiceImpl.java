// ================================================
// UserServiceImpl.java —— 用户业务层（实现类）
// 作用：实现 UserService 接口里定义的所有方法
// 会用在：Spring 自动注入到 UserController
// ================================================

package com.shop.service.impl;

import com.shop.dto.LoginRequest;
import com.shop.dto.RegisterRequest;
import com.shop.entity.User;
import com.shop.mapper.UserMapper;
import com.shop.service.UserService;
import com.shop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Service 标记这是业务层组件，Spring 会管理它
 * @RequiredArgsConstructor Lombok：为 final 字段生成构造方法（自动注入）
 *
 * BCryptPasswordEncoder：Spring Security 提供的密码加密工具
 * 虽然没引入 spring-boot-starter-security，但可以直接用这个类
 * 改进点：如果不想依赖这个类，可以用其他加密库
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // ===== 自动注入（构造方法注入，推荐方式） =====

    /**
     * final 字段 + @RequiredArgsConstructor
     * = Spring 自动把 UserMapper 的实现类注入进来
     *
     * 不用 @Autowired 的原因：构造方法注入更安全、方便测试
     */
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 注册流程详解：
     *   1. 检查用户名是否被占用
     *   2. 密码用 BCrypt 加密（不是明文存库！）
     *   3. 保存到数据库
     */
    @Override
    public void register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            // 抛出异常，GlobalExceptionHandler 会捕获并返回 Result.error()
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建新用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        // 加密密码：BCrypt 每次加密结果不一样，即使密码相同
        // 安全性：即使数据库泄露，也无法反解出原始密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 昵称：如果没填就用用户名代替
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        } else {
            user.setNickname(request.getUsername());
        }

        // 3. 保存到数据库
        // insert 是 MyBatis-Plus BaseMapper 自带的方法
        userMapper.insert(user);
    }

    /**
     * 登录流程详解：
     *   1. 根据用户名查用户
     *   2. 比对密码
     *   3. 生成 JWT Token
     */
    @Override
    public String login(LoginRequest request) {
        // 1. 查询用户是否存在
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 验证密码
        // matches(明文, 密文) → true/false
        // BCrypt 会从密文中提取盐值，然后用同样的方式加密明文再比较
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 生成 Token，返回给前端
        // 前端后续请求带着这个 Token 就能证明身份
        return jwtUtil.generateToken(user.getId());
    }

    /**
     * 根据 ID 获取用户信息
     * selectById 是 MyBatis-Plus 自带的方法
     *
     * 改进点：应该把密码置空再返回，避免密码泄露
     */
    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);  // 不让密码泄露出去
        }
        return user;
    }
}
