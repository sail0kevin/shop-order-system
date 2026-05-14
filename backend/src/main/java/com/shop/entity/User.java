// ================================================
// User.java —— 用户实体类（和数据库 user 表一一对应）
// 作用：每行数据对应数据库里的一条用户记录
// 会用在：MyBatis-Plus 查询结果自动封装成这个对象
// ================================================

package com.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 实体类 = 数据库表的"Java 映射"
 * 表的每一列对应类里的一个字段
 *
 * 类名 User → 表名 user（MyBatis-Plus 默认驼峰转下划线）
 * 如果表名不一样，可以用 @TableName("表名") 指定
 */
@Data  // Lombok：自动生成 getter/setter/equals/hashCode/toString
@TableName("user")  // MyBatis-Plus：指定对应哪张表
public class User {

    /**
     * 用户ID，自增主键
     * @TableId 标识这是主键
     * IDENTITY 表示数据库自增（MySQL 自动生成 ID）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;    // 用户名（登录用）

    private String password;    // 密码（加密后的密文，不是明文）

    private String nickname;    // 昵称（显示用）

    private String phone;       // 手机号

    private String email;       // 邮箱

    /**
     * 创建时间
     * @TableField(fill = ...) 可以自动填充值
     * 这里用数据库的 CURRENT_TIMESTAMP，所以不需要 MyBatis-Plus 自动填充
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * ON UPDATE CURRENT_TIMESTAMP，数据库自动更新
     */
    private LocalDateTime updateTime;
}
