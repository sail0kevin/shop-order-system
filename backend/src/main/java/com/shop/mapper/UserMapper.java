// ================================================
// UserMapper.java —— 用户数据库操作层
// 作用：定义对 user 表的增删改查方法
// 会用在：UserService 里调用这些方法操作数据库
// 原理：MyBatis-Plus 已经实现了基本的 CRUD
//       如果你需要写复杂 SQL，就在这个方法里声明
// ================================================

package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.User;

/**
 * UserMapper 继承 BaseMapper<User>
 * 继承后自动拥有以下方法（不用写任何代码）：
 *   - insert(User)     —— 插入一条用户记录
 *   - selectById(id)   —— 根据 ID 查用户
 *   - selectOne(...)   —— 按条件查一个
 *   - selectList(...)  —— 按条件查列表
 *   - updateById(...)  —— 根据 ID 更新
 *   - deleteById(id)   —— 根据 ID 删除
 *
 * 如果你想写复杂的 SQL 查询（比如多表关联），
 * 就在这里加方法，然后在 resources/mapper/UserMapper.xml 里写 SQL
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（登录时用）
     * 这里虽然 MyBatis-Plus 的 selectOne 也能实现，
     * 但单独写一个方法更清楚，方便以后扩展
     *
     * 改进点：如果只是简单查询，可以直接用 MyBatis-Plus 的
     *   LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
     *   wrapper.eq(User::getUsername, username);
     *   return baseMapper.selectOne(wrapper);
     * 不需要在 Mapper 里加方法
     */
    User findByUsername(String username);
}
