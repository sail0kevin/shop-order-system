-- ================================================
-- 数据库初始化脚本
-- 作用：创建数据库和项目需要的所有表
-- 怎么用：在 MySQL 命令行或 IDEA 数据库工具里执行
-- ================================================

-- 创建数据库（如果不存在）
-- shop 就是我们项目用的数据库名，和 application.yml 里一致
CREATE DATABASE IF NOT EXISTS shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用这个数据库
USE shop;

-- ================================================
-- 1. 用户表（user）
-- 作用：存储注册用户信息
-- 会用在：登录验证、用户信息查询
-- ================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID，自增主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名，登录时使用',
    `password`    VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt 加密后的密文）',
    `nickname`    VARCHAR(50)  DEFAULT NULL            COMMENT '昵称，显示用，可以为空',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)  -- 用户名唯一索引，避免重复注册
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ================================================
-- 2. 商品表（product）
-- 作用：存储商品信息
-- 会用在：商品列表、商品详情、购物车、订单
-- ================================================
CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`        VARCHAR(200)  NOT NULL                 COMMENT '商品名称',
    `description` TEXT          DEFAULT NULL             COMMENT '商品描述',
    `price`       DECIMAL(10,2) NOT NULL                 COMMENT '价格（元）',
    `stock`       INT           NOT NULL DEFAULT 0       COMMENT '库存数量',
    `image`       VARCHAR(500)  DEFAULT NULL             COMMENT '商品图片URL',
    `category`    VARCHAR(50)   DEFAULT NULL             COMMENT '分类（如：电子产品/服装/食品）',
    `status`      TINYINT       NOT NULL DEFAULT 1       COMMENT '状态：1-上架 0-下架',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),     -- 分类查询索引
    KEY `idx_name` (`name`)              -- 名称搜索索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ================================================
-- 3. 初始化商品数据（方便测试）
-- ================================================
INSERT INTO `product` (`name`, `description`, `price`, `stock`, `image`, `category`) VALUES
('iPhone 15 Pro', 'Apple A17 Pro 芯片，钛金属设计', 8999.00, 100, 'https://img.example.com/iphone15.jpg', '电子产品'),
('MacBook Air M3', '13.6 英寸 Liquid Retina 显示屏', 10999.00, 50, 'https://img.example.com/macbook.jpg', '电子产品'),
('耐克 Air Max 270', '气垫缓震运动鞋，舒适透气', 899.00, 200, 'https://img.example.com/nike.jpg', '服装'),
('经典纯棉T恤', '100% 纯棉，宽松版型', 99.00, 500, 'https://img.example.com/tshirt.jpg', '服装'),
('三只松鼠坚果礼盒', '每日坚果混合装 750g', 128.00, 300, 'https://img.example.com/nuts.jpg', '食品');

-- ================================================
-- 4. 订单表（orders）
-- 作用：存储每一笔订单的概要信息
-- 会用在：订单列表、订单详情查询
-- ================================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`    VARCHAR(32)   NOT NULL                 COMMENT '订单编号（唯一）',
    `user_id`     BIGINT        NOT NULL                 COMMENT '下单用户ID',
    `total_amount`DECIMAL(12,2) NOT NULL                 COMMENT '订单总金额',
    `status`      TINYINT       NOT NULL DEFAULT 0       COMMENT '订单状态：0-待支付 1-已支付 2-已发货 3-已完成',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ================================================
-- 5. 订单明细表（order_item）
-- 作用：存储订单中的每个商品
-- 会用在：订单详情展示（买了什么、多少钱）
-- ================================================
CREATE TABLE IF NOT EXISTS `order_item` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`    BIGINT        NOT NULL                 COMMENT '所属订单ID',
    `product_id`  BIGINT        NOT NULL                 COMMENT '商品ID',
    `product_name`VARCHAR(200)  NOT NULL                 COMMENT '商品名称（下单时的名称，冗余存储）',
    `price`       DECIMAL(10,2) NOT NULL                 COMMENT '购买时的单价',
    `quantity`    INT           NOT NULL                 COMMENT '购买数量',
    `subtotal`    DECIMAL(10,2) NOT NULL                 COMMENT '小计金额',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
