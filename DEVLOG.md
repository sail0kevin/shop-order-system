# 商城订单系统 — 开发日志

> 面试练手项目：Spring Boot + MyBatis-Plus + MySQL + Redis + JWT

---

## Day 1 — 项目搭建 + 用户模块

### 已完成

- [x] 项目脚手架搭建（Maven + Spring Boot 2.7.18 + JDK 11）
- [x] 统一响应格式 `Result<T>` + 全局异常处理
- [x] MySQL 用户表创建
- [x] 用户注册（BCrypt 密码加密）
- [x] 用户登录（JWT Token 签发）
- [x] JWT 拦截器（保护需登录接口）
- [x] Knife4j 接口文档

### 核心请求流程

```
请求 → JwtInterceptor(验证Token) → Controller(参数校验) → Service(业务逻辑) → Mapper(数据库)
                                         ↓ 出错
                              GlobalExceptionHandler → 统一返回 Result
```

### 文件结构与关系

```
pom.xml                         项目依赖清单（10个）
│
├── config/                      配置层
│   ├── BeanConfig.java         注册 BCryptPasswordEncoder（密码加密器）
│   ├── WebMvcConfig.java       注册 JwtInterceptor，配置放行路径
│   └── Knife4jConfig.java      Swagger 文档配置，全局 Token 传参
│
├── common/                      公共层
│   ├── Result.java             统一响应 {code, msg, data}，所有接口返回此格式
│   └── GlobalExceptionHandler.java  全局异常兜底，不把报错信息裸给前端
│
├── interceptor/
│   └── JwtInterceptor.java     安检：取 Header 的 Token → 验证 → 放行/拦截→ 存 userId
│
├── controller/
│   └── UserController.java     入口：/api/user/register | /login | /info
│
├── service/
│   ├── UserService.java        接口定义
│   └── impl/UserServiceImpl.java   实现：注册(加密存库) + 登录(验证+发Token)
│
├── mapper/
│   └── UserMapper.java         继承 BaseMapper，自带 CRUD + 自定义 findByUsername
│
├── entity/
│   └── User.java               映射 user 表字段
│
├── dto/
│   ├── LoginRequest.java       登录参数（含 @NotBlank 校验）
│   └── RegisterRequest.java    注册参数（含用户名长度校验）
│
├── util/
│   └── JwtUtil.java            JWT 工具：generateToken(签发) + validateToken(验证)
│
└── resources/
    ├── application.yml          数据库/Redis/JWT 配置
    └── mapper/UserMapper.xml    SQL：findByUsername
```

### 接口一览

| 方法 | 地址 | 说明 | 需登录 |
|------|------|------|--------|
| POST | /api/user/register | 注册 | 否 |
| POST | /api/user/login | 登录，返回 token | 否 |
| GET | /api/user/info | 获取当前用户信息 | 是 |

### 关键技术点

- **密码加密**：BCrypt（不可逆，每次密文不同），比 MD5 安全
- **JWT 结构**：Header.Payload.Signature，服务端无状态，Token 存在客户端
- **拦截器放行**：注册/登录接口不拦截，其余 /api/ 接口都要 Token
- **统一异常处理**：`@RestControllerAdvice` + `@ExceptionHandler` 分层捕获

### ⚠️ 启动前需确认

1. MySQL 服务运行中
2. 执行 `sql/init.sql` 创建数据库和 user 表
3. `application.yml` 里的 MySQL 密码改成你自己的

---

## Day 2 — 商品模块 + Redis 缓存

### 已完成

- [x] 商品表创建 + 5条测试数据（电子产品/服装/食品）
- [x] 商品分页查询（MyBatis-Plus Page）
- [x] 按分类筛选、关键词搜索
- [x] 商品详情（带 Redis 缓存）
- [x] MyBatis-Plus 分页插件配置

### 新增文件

| 文件 | 作用 |
|------|------|
| `entity/Product.java` | 商品实体（id/name/price/stock/image/category/status） |
| `mapper/ProductMapper.java` | 继承 BaseMapper，自带分页和 CRUD |
| `service/ProductService.java` | 分页查询 + 详情（含缓存） |
| `service/impl/ProductServiceImpl.java` | 实现：MyBatis-Plus 条件构造器 + StringRedisTemplate 缓存 |
| `controller/ProductController.java` | GET /api/product/list + /detail/{id} |
| `config/MyBatisPlusConfig.java` | 注册 PaginationInnerInterceptor（分页插件） |

### 新增接口

| 方法 | 地址 | 说明 | 需登录 |
|------|------|------|--------|
| GET | /api/product/list?page=1&size=10&category=&keyword= | 商品列表 | 是 |
| GET | /api/product/detail/1 | 商品详情 | 是 |

### Redis 缓存设计

```
key = "product:detail:{id}"      例如 "product:detail:1"
value = 商品 JSON 字符串
过期时间 = 3600 秒（1小时）

读写流程：
  请求 → 查 Redis → 有？→ 直接返回（不查数据库）
                  → 没有？→ 查 MySQL → 写入 Redis → 返回
```

### 分页查询说明

请求 `/api/product/list?page=1&size=3&category=电子产品`

返回格式：
```json
{
  "records": [ ...当前页商品... ],
  "total": 5,
  "current": 1,
  "pages": 2
}
```

### 已开服务器

- 端口 8080（Tomcat）
- Redis 缓存已就绪（6379）

---

## Day 3 — 购物车 + 下单事务

### 已完成

- [x] Redis Hash 购物车（增删改查 + 商品信息联查）
- [x] 下单事务（@Transactional + 库存扣减 + 超卖防护）
- [x] 订单表 + 订单明细表
- [x] 订单列表（分页）

### 新增文件

| 文件 | 作用 |
|------|------|
| `service/CartService.java` | 购物车接口 + CartItemVO 内部类 |
| `service/impl/CartServiceImpl.java` | Redis Hash 操作（HGETALL/HSET/HDEL） |
| `controller/CartController.java` | POST /add /update /remove + GET /list |
| `entity/Order.java` | 订单实体（orderNo/totalAmount/status） |
| `entity/OrderItem.java` | 订单明细（productName/price/quantity/subtotal） |
| `mapper/OrderMapper.java` | 订单 Mapper |
| `mapper/OrderItemMapper.java` | 订单明细 Mapper |
| `service/OrderService.java` | 下单 + 订单列表接口 |
| `service/impl/OrderServiceImpl.java` | @Transactional 事务下单核心实现 |
| `controller/OrderController.java` | POST /create + GET /list |

### 新增接口

| 方法 | 地址 | 说明 | 需登录 |
|------|------|------|--------|
| POST | /api/cart/add | 添加商品到购物车 | 是 |
| POST | /api/cart/update | 修改数量 | 是 |
| POST | /api/cart/remove | 删除商品 | 是 |
| GET | /api/cart/list | 查看购物车 | 是 |
| POST | /api/order/create | 从购物车下单 | 是 |
| GET | /api/order/list?page=1&size=10 | 订单列表 | 是 |

### 下单事务流程（面试重点）

```
1. 获取购物车数据（Redis）
2. 验证库存（MySQL）
3. 扣减库存（UPDATE ... WHERE stock >= ?）
4. 生成订单编号
5. 插入订单表 + 明细表
6. 清空购物车（Redis）
   └── 以上全部在一个 @Transactional 中
```

### 超卖防护设计

扣库存 SQL：
```sql
UPDATE product SET stock = stock - 1 WHERE id = ? AND stock >= ?
```
`WHERE stock >= ?` 保证库存不足时影响行数为 0，配合 `affected == 0` 抛出异常回滚事务。

### 当前项目结构

```
商城系统/
├── common/     Result.java + GlobalExceptionHandler.java
├── config/     BeanConfig + WebMvcConfig + Knife4jConfig + MyBatisPlusConfig
├── controller/ UserController + ProductController + CartController + OrderController
├── service/    UserService + ProductService + CartService + OrderService
├── mapper/     UserMapper + ProductMapper + OrderMapper + OrderItemMapper (Cart用Redis)
├── entity/     User + Product + Order + OrderItem
├── dto/        LoginRequest + RegisterRequest
├── interceptor/ JwtInterceptor
├── util/       JwtUtil
└── sql/        init.sql
```
