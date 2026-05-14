# Shop Order System — 商城订单系统

Spring Boot + MyBatis-Plus + MySQL + Redis + JWT 全栈实战项目。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 项目框架（IoC、MVC、事务） |
| MyBatis-Plus | 3.5.5 | ORM 框架（单表 CRUD 零 SQL） |
| MySQL | 8.0+ | 持久化存储（用户、商品、订单） |
| Redis | 6.x+ | 缓存（商品详情、购物车） |
| JWT (java-jwt) | 4.4.0 | 无状态登录认证 |
| Knife4j | 4.3.0 | API 接口文档（/doc.html） |
| JDK | 11+ | 运行环境 |

## 项目结构

```
src/main/java/com/shop/
├── common/                  # 公共层
│   ├── Result.java          # 统一响应格式 {code, msg, data}
│   └── GlobalExceptionHandler.java  # 全局异常处理
├── config/                  # 配置层
│   ├── BeanConfig.java      # BCrypt 密码加密器
│   ├── WebMvcConfig.java    # JWT 拦截器注册
│   ├── MyBatisPlusConfig.java  # 分页插件
│   └── Knife4jConfig.java   # Swagger 文档配置
├── controller/              # 接口层（4个控制器）
│   ├── UserController.java   # /api/user/**
│   ├── ProductController.java # /api/product/**
│   ├── CartController.java    # /api/cart/**
│   └── OrderController.java   # /api/order/**
├── service/                 # 业务层
│   └── impl/                # 业务实现
├── mapper/                  # 数据访问层
├── entity/                  # 数据实体
├── dto/                     # 数据传输对象
├── interceptor/             # JWT 拦截器
└── util/                    # 工具类
    ├── JwtUtil.java         # JWT 签发与验证
    └── SnowflakeIdUtil.java # 雪花算法 ID 生成器
```

## 功能模块

### 1. 用户模块
- **注册** — BCrypt 加密存储密码
- **登录** — 签发 JWT Token（24h 过期）
- **信息查询** — 拦截器鉴权后获取用户信息

### 2. 商品模块
- **分页查询** — MyBatis-Plus Page，支持分类筛选 + 关键词搜索
- **商品详情** — Redis 缓存（key=`product:detail:{id}`，TTL=1h）

### 3. 购物车（Redis Hash）
- 基于 Redis Hash 结构，key=`cart:{userId}`
- 增删改查，自动联查商品最新信息

### 4. 订单模块
- **下单（事务核心）** — `@Transactional` + `WHERE stock >= ?` 防超卖
- **支付（模拟）** — 状态更新 待支付 → 已支付
- **订单列表** — 分页按时间倒序

## API 接口一览

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | /api/user/register | 用户注册 | 否 |
| POST | /api/user/login | 登录，返回 token | 否 |
| GET | /api/user/info | 获取当前用户信息 | 是 |
| GET | /api/product/list | 商品分页列表 | 是 |
| GET | /api/product/detail/{id} | 商品详情 | 是 |
| POST | /api/cart/add | 添加商品到购物车 | 是 |
| POST | /api/cart/update | 修改购物车数量 | 是 |
| POST | /api/cart/remove | 删除购物车商品 | 是 |
| GET | /api/cart/list | 查看购物车 | 是 |
| POST | /api/order/create | 从购物车下单 | 是 |
| POST | /api/order/pay | 支付订单 | 是 |
| GET | /api/order/list | 订单列表 | 是 |

## 核心难点

### 超卖防护
```sql
UPDATE product SET stock = stock - 1 WHERE id = ? AND stock >= ?
```
利用 MySQL 行锁 + 条件更新保证并发安全：两条请求同时到达时，第二条因 `stock >= ?` 不成立而影响 0 行，触发事务回滚。

### 下单事务
```java
@Transactional(rollbackFor = Exception.class)
```
购物车 → 验库存 → 扣库存 → 生成订单号 → 插订单表 → 插明细表 → 清购物车，任一环节失败全部回滚。

### 雪花算法 ID
订单编号使用雪花算法，全局唯一、趋势递增、高性能，取代 Random 方案。

## 快速启动

### 前置条件
- JDK 11+、Maven 3.6+
- MySQL 8.0+（端口 3306）
- Redis 6.x+（端口 6379）

### 启动步骤

```bash
# 1. 初始化数据库
mysql -u root -p < sql/init.sql

# 2. 修改数据库密码（application.yml 中 spring.datasource.password）

# 3. 启动项目
mvn spring-boot:run

# 4. 访问接口文档
# http://localhost:8080/doc.html
```

项目默认端口 **8080**，详见 `application.yml`。

## 测试

```bash
mvn test
```

测试覆盖：用户注册登录、商品分页/详情/缓存、购物车增删改、下单事务、支付流程。
