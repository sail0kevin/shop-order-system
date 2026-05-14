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

---

## Day 4 — 前后端页面 + 管理后台 + 订单支付

### 已完成

- [x] 项目重构为 monorepo 结构（backend/ + frontend/）
- [x] JWT 双 Token 认证（access_token 24h + refresh_token 7d）
- [x] Token 自动刷新（401 → /api/user/refresh → 重试原请求）
- [x] 雪花算法订单编号（SnowflakeIdUtil，替代原随机数方案）
- [x] 订单支付功能（POST /api/order/pay，状态 0→1）
- [x] CORS 全局过滤器（CorsConfig，允许前端跨域）
- [x] 商城前端页面（index.html，LUXEMALL 轻奢风格）
- [x] 管理后台页面（admin.html，完整 SPA 管理系统）
- [x] 管理后台 API（AdminController — 统计/图表/用户/订单）
- [x] Vue3 SPA 前端项目（frontend/，Vite + Element Plus + Pinia）
- [x] 结算自动支付（下单→支付一气呵成，便于观察数据变化）

### 新增文件

| 文件 | 作用 |
|------|------|
| `config/CorsConfig.java` | CORS 过滤器，允许所有来源跨域请求 |
| `controller/AdminController.java` | 管理后台 API（stats/chart/users/orders） |
| `util/SnowflakeIdUtil.java` | 雪花算法 ID 生成器（datacenterId=6, workerId 随机） |
| `resources/static/index.html` | 商城前端页面（LUXEMALL 轻奢设计） |
| `resources/static/admin.html` | 管理后台页面（仪表盘+用户+商品+订单管理） |

### 修改文件

| 文件 | 变更内容 |
|------|---------|
| `util/JwtUtil.java` | 新增 generateRefreshToken() / refreshAccessToken()，Token 增加 type 声明 |
| `controller/UserController.java` | login 返回双 Token，新增 POST /api/user/refresh 刷新接口 |
| `service/impl/OrderServiceImpl.java` | 新增 payOrder() 支付方法，订单编号改为雪花算法 |
| `service/OrderService.java` | 新增 payOrder() 接口定义 |
| `service/impl/ProductServiceImpl.java` | getProductDetail 商品不存在时抛异常（之前返回 null） |
| `pom.xml` | 移至 backend/ 目录 |
| `Dockerfile` | 移至 backend/ 目录 |
| `docker-compose.yml` | 路径更新为 ./backend |
| `README.md` | 更新项目结构说明 |

### 新增接口

| 方法 | 地址 | 说明 | 需登录 |
|------|------|------|--------|
| POST | /api/order/pay | 支付订单（模拟） | 是 |
| POST | /api/user/refresh | 刷新 Token | 否 |
| GET | /api/admin/stats | 管理后台统计（销售额/订单/用户/商品数） | 是 |
| GET | /api/admin/chart | 近 7 日收入趋势 | 是 |
| GET | /api/admin/orders | 全部订单列表（分页） | 是 |
| GET | /api/admin/users | 全部用户列表（分页） | 是 |

### 页面访问地址

| 页面 | 地址 |
|------|------|
| 商城首页 | http://localhost:8080/ |
| 管理后台 | http://localhost:8080/admin.html |
| API 文档 | http://localhost:8080/doc.html |

### 商城前端功能

- 登录/注册弹窗 → /api/user/login, /api/user/register
- 商品列表（分页+分类筛选+关键词搜索） → /api/product/list
- 鼠标悬停商品 → 加入购物车 → /api/cart/add
- 购物车侧栏（数量加减+删除） → /api/cart/list, /api/cart/update, /api/cart/remove
- 结算（自动下单+支付） → /api/order/create → /api/order/pay
- 订单列表+支付 → /api/order/list, /api/order/pay
- 导航栏登录状态切换、购物车徽标实时更新

### 管理后台功能

- 登录认证（JWT）
- 仪表盘：销售额/订单/商品/用户统计卡片 + 7日收入趋势图（Chart.js）
- 用户管理：全部用户列表，分页浏览
- 商品管理：全部商品列表，支持分类筛选和搜索，分页浏览
- 订单列表：全部订单（含状态标签），分页浏览
- 侧栏 Tab 切换，响应式布局（移动端适配）

### 关键技术点

- **双 Token 机制**：access_token 用于 API 鉴权（24h），refresh_token 用于无感续期（7d）。前端 Axios 拦截器检测 401 自动调用 refresh 接口
- **雪花算法**：订单编号使用 Twitter Snowflake 算法，全局唯一、趋势递增、高性能（409.6 万/秒）
- **结算即支付**：演示项目为方便观察数据变化，结算按钮自动完成下单+支付，订单直接进入"已支付"状态
- **管理后台 SPA**：纯原生 JS 实现 Tab 切换+分页，无框架依赖，与商城共用同一数据库

### Bug 修复记录

- `ProductServiceImpl.getProductDetail` 商品不存在时抛出 RuntimeException（之前返回 null 导致前端无法区分）
- 测试文件移除 `@TestMethodOrder` / `@Order` 注解（`com.shop.entity.Order` 与 JUnit `@Order` 冲突）
- 管理后台初始版本全为 mock 假数据，已全部接入真实 API
- `showOrders()` 覆盖 `#section-products` 导致商品列表消失，已拆分为独立 section
- 旧订单状态未支付数据已批量修正
