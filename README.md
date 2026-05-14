# Shop Order System — 商城订单系统

> Spring Boot + MyBatis-Plus + MySQL + Redis + JWT 全栈实战项目。
> 适合用来学习和面试展示。

---

## 用到的技术

| 技术 | 版本 | 有什么用 |
|------|------|---------|
| Spring Boot | 2.7.18 | 项目框架（IoC 控制反转、MVC、事务管理） |
| MyBatis-Plus | 3.5.5 | ORM 框架，单表不用写 SQL |
| MySQL | 8.0+ | 存数据（用户、商品、订单） |
| Redis | 6.x+ | 缓存（商品详情、购物车） |
| JWT (java-jwt) | 4.4.0 | 登录认证，无状态 Token |
| Knife4j | 4.3.0 | API 文档，浏览器打开就能调试接口 |
| JDK | 11+ | 运行环境 |

## 项目结构

```
├── backend/                    # 后端代码（Spring Boot + Maven）
│   ├── pom.xml                 # 项目依赖清单
│   ├── sql/init.sql            # 数据库建表脚本
│   ├── Dockerfile              # Docker 构建文件
│   └── src/main/java/com/shop/
│       ├── common/             # 公共层：统一返回格式 + 全局异常处理
│       ├── config/             # 配置：CORS / MyBatis-Plus / Knife4j
│       ├── controller/         # 控制器层：接收请求 → 返回结果
│       ├── service/            # 业务逻辑层
│       ├── mapper/             # 数据访问层
│       ├── entity/             # 实体类（映射数据库表）
│       ├── dto/                # 请求参数封装
│       ├── interceptor/        # JWT 拦截器（验证 Token）
│       └── util/               # 工具类（JWT + 雪花算法）
│
├── frontend/                   # Vue3 前端代码（可选）
│   └── src/
│       ├── api/                # Axios 封装 + 各模块 API 调用
│       ├── stores/             # Pinia 状态管理
│       ├── router/             # 路由配置
│       ├── components/         # 公共组件
│       └── views/              # 页面
│
├── docker-compose.yml          # 一键部署（MySQL + Redis + 项目）
└── README.md
```

## 功能模块

### 1. 用户模块

做什么的？
- 注册账号、登录、查询个人信息

核心逻辑：
- **注册** → 用户名 + 密码 → BCrypt 加密存库
- **登录** → 验证密码 → 签发 JWT 双 Token（access_token 24h 过期 + refresh_token 7天过期）
- **Token 刷新** → 前端检测到 401 会自动调用刷新接口，用户无感续期

### 2. 商品模块

做什么的？
- 浏览商品、按分类筛选、搜关键词、看详情

核心逻辑：
- **分页查询** — MyBatis-Plus Page 插件，支持 category 筛选 + keyword 模糊搜索
- **商品详情** — 先查 Redis（key=`product:detail:{id}`），没有再查 MySQL，写回 Redis（TTL=1h）

### 3. 购物车模块

做什么的？
- 加商品、改数量、删商品、查看购物车

核心逻辑：
- 存在 Redis Hash 里，key=`cart:{userId}`，field=`productId`，value=`数量`
- 查询时联查 MySQL 商品表获取最新价格和名称

### 4. 订单模块

做什么的？
- 下单（前端点结算）、支付（模拟）、查看订单列表

核心逻辑：
- **下单** — 购车车 → 验库存 → 扣库存 → 插订单 → 插明细 → 清购物车，全程事务，出问题全部回滚
- **超卖防护** — `UPDATE product SET stock = stock - 1 WHERE id = ? AND stock >= ?`，条件不满足时影响 0 行，触发回滚
- **支付** — 把订单状态从 0（待支付）改成 1（已支付）
- **订单编号** — 雪花算法生成，全局唯一、趋势递增

### 5. 前端页面

做什么的？
- 商城首页 — 给用户用的，登录 → 看商品 → 加购物车 → 结算 → 支付
- 管理后台 — 给管理员用的，看统计图表、管理用户/商品/订单

页面地址：
| 页面 | 地址 |
|------|------|
| 商城首页 | http://localhost:8080/ |
| 管理后台 | http://localhost:8080/admin.html |
| API 文档 | http://localhost:8080/doc.html |

## API 接口一览

### 用户模块
| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | /api/user/register | 注册账号 | 否 |
| POST | /api/user/login | 登录，返回 token | 否 |
| GET | /api/user/info | 获取当前用户信息 | 是 |
| POST | /api/user/refresh | 用 refresh_token 换新的 access_token | 否 |

### 商品模块
| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| GET | /api/product/list | 商品列表（支持 page/size/category/keyword） | 是 |
| GET | /api/product/detail/{id} | 商品详情（Redis 缓存） | 是 |

### 购物车模块
| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | /api/cart/add | 添加到购物车 | 是 |
| POST | /api/cart/update | 修改数量 | 是 |
| POST | /api/cart/remove | 删除购物车商品 | 是 |
| GET | /api/cart/list | 查看购物车（联查最新商品信息） | 是 |

### 订单模块
| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | /api/order/create | 从购物车下单（事务） | 是 |
| POST | /api/order/pay | 支付订单（模拟） | 是 |
| GET | /api/order/list | 我的订单列表 | 是 |

### 管理后台
| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| GET | /api/admin/stats | 统计面板数据（销售额/订单数/用户数/商品数） | 是 |
| GET | /api/admin/chart | 近 7 日收入趋势 | 是 |
| GET | /api/admin/orders | 全部订单列表 | 是 |
| GET | /api/admin/users | 全部用户列表 | 是 |

## 核心难点（面试常问）

### 1. 超卖防护

扣库存的 SQL 写法是：
```sql
UPDATE product SET stock = stock - 1 WHERE id = ? AND stock >= ?
```
关键在 `AND stock >= ?` — 如果库存不够，这行 SQL 影响 0 行数据，程序检测到 `affected == 0` 就抛异常，事务回滚。

两条请求同时进来时：
- MySQL 行锁让第二条等
- 第一条扣完，stock 变少了
- 第二条发现 `stock >= ?` 不成立，影响 0 行 → 回滚

### 2. 下单事务

```java
@Transactional(rollbackFor = Exception.class)
public Order createOrder(Long userId) {
    // 1. 拿购物车数据（Redis）
    // 2. 验证库存（MySQL）
    // 3. 扣库存（UPDATE ... AND stock >= ?）
    // 4. 生成订单编号（雪花算法）
    // 5. 插入订单表
    // 6. 插入订单明细表
    // 7. 清空购物车（Redis）
    // → 以上要么全成功，要么全回滚
}
```

### 3. JWT 双 Token

- **access_token** — 有效期 24h，放在请求头 `Authorization: Bearer xxx`，用来鉴权
- **refresh_token** — 有效期 7 天，access_token 过期后用它换新的，避免用户频繁登录

Token 里存了什么？
```json
{
  "user_id": 1,
  "type": "access",      // 或 "refresh"
  "iat": 1670000000,     // 签发时间
  "exp": 1670086400      // 过期时间
}
```

### 4. 雪花算法

订单编号用雪花算法生成，不用数据库自增 ID 或 Random：
- **全局唯一** — 不会重复
- **趋势递增** — 数据库索引效率高
- **高性能** — 409.6 万/秒
- **可反推时间** — 从 ID 能知道什么时候生成的

## 快速启动

### 需要装什么
- JDK 11+（运行 Java）
- Maven 3.6+（构建项目）
- MySQL 8.0+（数据库，端口 3306）
- Redis 6.x+（缓存，端口 6379）

### 启动步骤

```bash
# 1. 初始化数据库
mysql -u root -p < backend/sql/init.sql

# 2. 改密码（backend/src/main/resources/application.yml 里把数据库密码改成你自己的）

# 3. 启动后端（会自动初始化 5 条商品测试数据）
cd backend && mvn spring-boot:run

# 4. 新开终端，启动 Vue3 前端（可选）
cd frontend && npm install && npm run dev

# 5. 打开浏览器访问
#    商城首页： http://localhost:8080/
#    管理后台： http://localhost:8080/admin.html
#    API 文档： http://localhost:8080/doc.html
```

项目默认端口 **8080**。

## 测试

```bash
cd backend && mvn test
```

测试覆盖：用户注册登录、商品分页/详情/缓存、购物车增删改查、下单事务、支付流程。
