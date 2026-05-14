# API 文档使用指南 —— 从入门到理解

> 本文档解释 `http://localhost:8080/doc.html` 这个页面上的一切。
> 如果你是后端初学者，这份指南帮你搞懂"这个页面到底是什么、怎么用、每个按钮是干嘛的"。

---

## 一、这个页面到底是什么？

### 简单理解

你打开 `http://localhost:8080/doc.html` 看到的页面，叫做 **API 文档页面**。

它不是给普通用户看的商城页面，而是**给开发者（你）看的接口说明书**。

```
类比：
  你买了一个新手机 → 里面有一本说明书
  说明书告诉你："这个按钮是开关机，这个接口是充电口"

  API 文档 = 你项目的说明书
  它告诉你："这个接口是注册用户，这个接口是查商品列表"
```

### 为什么要有这个页面？

你在后端写了接口（比如登录接口），前端开发人员需要知道：
- 这个接口地址是什么？（`/api/user/login`）
- 要传什么参数？（用户名 + 密码）
- 返回什么格式？（`{"code":200, "data": {"token":"..."}`）
- 要不要登录才能调？

API 文档自动读取你代码里的注解（`@GetMapping`、`@PostMapping` 这些），把这些信息整理成页面展示出来。

---

## 二、页面整体布局

```
┌──────────────────────────────────────────────────────────┐
│  ☰  商城订单系统 API 文档              [Authorize] 按钮    │ ← 顶栏
│  包含用户、商品、购物车、订单模块                        │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌─ 接口列表（左边） ────────────────────────────────── │
│  │                                                      │ │
│  │  ▼ cart-controller（购物车）                         │ │
│  │    POST /api/cart/add                                │ │
│  │    POST /api/cart/update                             │ │
│  │    POST /api/cart/remove                             │ │
│  │    GET  /api/cart/list                               │ │
│  │                                                      │ │
│  │  ▼ order-controller（订单）                          │ │
│  │    POST /api/order/create                            │ │
│  │    GET  /api/order/list                              │ │
│  │                                                      │ │
│  │  ▼ product-controller（商品）                        │ │
│  │    GET  /api/product/list                            │ │
│  │    GET  /api/product/detail/{id}                     │ │
│  │                                                      │ │
│  │  ▼ user-controller（用户）                           │ │
│  │    POST /api/user/register                           │ │
│  │    POST /api/user/login                              │ │
│  │    GET  /api/user/info                               │ │
│  │                                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                          │
│  右边区域：点击某个接口后，这里显示详细信息              │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 页面两部分

| 区域 | 位置 | 作用 |
|------|------|------|
| **接口列表** | 左边 | 展示所有接口，按功能分组 |
| **接口详情** | 右边 | 点某个接口后，展示它的参数、返回值、调试功能 |

---

## 三、4 个"大栏目"（接口分组）

左边列表有 4 个控制器分组，每个对应你项目里的一个 Controller.java 文件：

| 大栏目 | 对应 Java 文件 | 负责什么 |
|--------|---------------|---------|
| **user-controller** | UserController.java | 用户相关：注册、登录、查信息 |
| **product-controller** | ProductController.java | 商品相关：列表、详情 |
| **cart-controller** | CartController.java | 购物车相关：增删改查 |
| **order-controller** | OrderController.java | 订单相关：下单、查询 |

---

## 四、每个小栏目（接口）详解

---

### 4.1 user-controller（用户模块）

#### ① POST /api/user/register —— 注册

```
作用：创建一个新用户账号
位置：UserController.java 第 50 行

请求体（要传什么）：
{
  "username": "kevin",        ← 用户名（必填，3-20个字符）
  "password": "123456",       ← 密码（必填，6-32个字符）
  "nickname": "凯文"          ← 昵称（选填，不填默认等于用户名）
}

返回结果（服务器会给你什么）：
{
  "code": 200,                ← 200 表示成功
  "msg": "成功",               ← 提示信息
  "data": null                ← 没有额外数据
}

执行流程（代码里实际发生了什么）：
  ① 收到你的请求
  ② 检查用户名是否已被注册
  ③ 用 BCrypt 加密密码（不是明文存数据库）
  ④ 把用户信息存到 MySQL 的 user 表
  ⑤ 返回 "注册成功"

注意点：
  - 注册不需要登录（不需要传 Token）
  - 密码是加密存储的，即使数据库泄露也拿不到原始密码
  - 如果用户名已存在，返回 code=400+"用户名已存在"
```

#### ② POST /api/user/login —— 登录

```
作用：登录已有账号，获取 JWT Token
位置：UserController.java 第 72 行

请求体：
{
  "username": "kevin",
  "password": "123456"
}

返回结果：
{
  "code": 200,
  "msg": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs..."   ← 这个就是 JWT Token
  }
}

Token 是干什么用的？
  Token = 你的"临时身份证"
  登录成功后拿到 Token
  之后调用其他接口（购物车、下单）都需要带上它
  证明"我是 kevin，我登录过了"

Token 长什么样？
  eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
  eyJ1c2VyX2lkIjoxLCJpYXQiOjE3Nzg3Mzc1OTAsImV4cCI6MTc3ODgyMzk5MH0.
  ro3Pa6bBV8_Y0vxvDGyrVo_O2kUrhyiG_PwDI1zILyg
  
  三部分用点隔开：
  ① 头部（Header）—— 用了什么加密算法
  ② 载荷（Payload）—— 存了用户ID、签发时间、过期时间
  ③ 签名（Signature）—— 防伪标识，防止别人篡改

执行流程：
  ① 查数据库有没有这个用户名
  ② 用 BCrypt 比对密码对不对
  ③ 生成 JWT Token（里面存了用户ID，24小时过期）
  ④ 返回 Token

注意：
  - 登录也不需要传 Token（还没登录哪来的 Token）
  - 用户名或密码错误 → 返回 "用户名或密码错误"
```

#### ③ GET /api/user/info —— 获取用户信息

```
作用：查看当前登录用户的个人信息
位置：UserController.java 第 101 行

请求地址：GET /api/user/info
（不需要传请求体，但需要 Token）

请求头：
  Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

返回结果：
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "kevin",
    "password": null,        ← 密码被置空了，不会泄露
    "nickname": "kevin",
    "phone": null,
    "email": null,
    "createTime": "2026-05-14T12:46:30",
    "updateTime": "2026-05-14T12:46:30"
  }
}

注意：
  - 需要登录（要传 Token）
  - 密码字段是 null，安全考虑不返回密码
  - 这个接口验证 Token 是否有效的同时，还能拿到用户信息
```

---

### 4.2 product-controller（商品模块）

#### ① GET /api/product/list —— 商品列表

```
作用：查看所有上架的商品，支持分页、分类筛选、关键词搜索
位置：ProductController.java 第 44 行

请求地址（参数都在 URL 里）：
  GET /api/product/list?page=1&size=5&category=电子产品&keyword=iPhone

参数说明：
  page     = 1             ← 第几页（从1开始）
  size     = 5             ← 每页显示多少条
  category = 电子产品      ← 按分类筛选（可选）
  keyword  = iPhone        ← 按名称搜索（可选）

返回结果：
{
  "code": 200,
  "data": {
    "records": [                        ← 当前页的商品
      {
        "id": 1,
        "name": "iPhone 15 Pro",
        "description": "Apple A17 Pro 芯片，钛金属设计",
        "price": 8999.00,
        "stock": 100,
        "image": "https://img.example.com/iphone15.jpg",
        "category": "电子产品",
        "status": 1
      },
      ...
    ],
    "total": 5,                         ← 总共有多少条商品
    "size": 5,                          ← 每页多少条
    "current": 1,                       ← 当前第几页
    "pages": 1                          ← 总共有多少页
  }
}

实用技巧（URL 参数组合）：
  /api/product/list?page=1&size=10              ← 第1页，每页10条
  /api/product/list?category=电子产品            ← 只看电子产品的商品
  /api/product/list?keyword=iPhone               ← 搜索名字包含 iPhone 的商品
  /api/product/list?page=2&size=3&category=服装  ← 服装类第2页，每页3条

注意：
  - 需要登录（传 Token）
  - 只返回 status=1（上架）的商品
  - 商品按上架时间倒序排列（最新的在前面）
```

#### ② GET /api/product/detail/{id} —— 商品详情

```
作用：查看某个商品的详细信息
位置：ProductController.java 第 66 行

请求地址（把 {id} 换成实际的商品ID）：
  GET /api/product/detail/1    ← 查 ID=1 的商品

返回结果：
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "Apple A17 Pro 芯片，钛金属设计",
    "price": 8999.00,
    "stock": 97,
    "image": "https://img.example.com/iphone15.jpg",
    "category": "电子产品",
    "status": 1,
    ...
  }
}

背后的缓存机制（面试考点）：
  第一次查 /detail/1：
    Redis 里没有 → 查 MySQL → 把结果存到 Redis（key=product:detail:1）
    耗时：约 50ms
  
  第二次查 /detail/1：
    Redis 里有 → 直接返回（不用查 MySQL）
    耗时：约 5ms（快10倍）

  Redis 缓存过期时间：1小时
  1小时后再次查询 → 重新从 MySQL 拉数据 → 再缓存

注意：
  - 需要登录
  - 商品不存在会返回 "商品不存在"
```

---

### 4.3 cart-controller（购物车模块）

#### ① POST /api/cart/add —— 添加到购物车

```
作用：把某个商品加入购物车
位置：CartController.java 第 24 行

请求体：
{
  "productId": 1,         ← 商品ID（必填）
  "quantity": 2           ← 数量（必填）
}

数据存在哪里？
  不是 MySQL，而是 Redis！
  Redis key = "cart:1"（1 是用户ID）
  结构 = Hash（键值对集合）
  存的内容 = { "1": "2" }   ← 商品ID=1，数量=2
  
  如果再次添加同样的商品（productId=1），数量会累加：
  { "1": "2" } → 再添加 productId=1, quantity=3 → { "1": "5" }

返回：
  {"code":200,"msg":"成功","data":null}
```

#### ② POST /api/cart/update —— 修改数量

```
作用：直接设置购物车中某个商品的数量（不是累加，是覆盖）
位置：CartController.java 第 40 行

请求体：
{
  "productId": 1,         ← 要修改的商品
  "quantity": 5           ← 新数量（直接覆盖旧值）
}

场景：
  购物车有 iPhone × 2
  调 update(productId=1, quantity=5)
  → 变成 iPhone × 5（不是 2+5=7）
```

#### ③ POST /api/cart/remove —— 删除商品

```
作用：从购物车里移除某个商品
位置：CartController.java 第 56 行

请求体：
{
  "productId": 1         ← 要删除的商品ID
}
```

#### ④ GET /api/cart/list —— 查看购物车

```
作用：查看购物车里所有商品（带详细信息）
位置：CartController.java 第 69 行

返回结果：
{
  "code": 200,
  "data": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",        ← 从 MySQL 查到的商品名
      "productImage": "https://...",         ← 商品图片
      "price": 8999.00,                      ← 单价
      "quantity": 2,                         ← 数量（从 Redis 取）
      "subtotal": 17998.00                   ← 小计（单价×数量）
    }
  ]
}

数据怎么组装出来的？
  ① 从 Redis 取：HGETALL cart:1 → { "1": "2", "3": "1" }
  ② 取出商品ID列表 [1, 3]
  ③ 从 MySQL 查商品信息：SELECT * FROM product WHERE id IN (1, 3)
  ④ 把 Redis 的数量 + MySQL 的商品信息拼在一起
  ⑤ 返回给前端
```

---

### 4.4 order-controller（订单模块）

#### ① POST /api/order/create —— 下单（最核心的功能）

```
作用：从购物车生成订单，扣减库存
位置：OrderController.java 第 27 行

请求体：不需要！直接从购物车取数据

完整的下单流程（面试重点，务必理解）：
  
  第1步：获取购物车数据（Redis）
    从 Redis 的 cart:1 取出所有商品和数量
  
  第2步：验证库存
    遍历购物车里每个商品，查 MySQL 看库存够不够
    库存不够 → 抛出异常，下单失败
  
  第3步：扣减库存（关键）
    执行 SQL：
    UPDATE product SET stock = stock - 购买数量
    WHERE id = 商品ID AND stock >= 购买数量
    
    注意 WHERE stock >= 购买数量 的作用：
    如果两个用户同时购买同一个商品：
      MySQL 的行锁会让第二个请求等待
      第一个扣完库存，第二个发现库存不够
      affected = 0（没有行被更新）
      → 抛出"库存不足"异常
  
  第4步：计算总价
    所有商品的小计相加
  
  第5步：生成订单编号
    格式：20260514 + 6位随机数（如 20260514429218）
  
  第6步：保存订单
    插入 orders 表（订单概要：总价、状态、用户ID）
    插入 order_item 表（订单明细：每个商品的名称、数量、单价）
  
  第7步：清空购物车
    删除 Redis 的 cart:1

  以上 7 步在 @Transactional 事务中执行：
    任何一步失败 → 全部撤销（库存不扣、订单不创建）
    保证数据一致性

返回结果：
{
  "code": 200,
  "data": {
    "id": 1,
    "orderNo": "20260514429218",        ← 订单编号
    "userId": 1,
    "totalAmount": 28193.00,            ← 总金额
    "status": 0,                         ← 状态：0=待支付
    ...
  }
}
```

#### ② GET /api/order/list —— 订单列表

```
作用：查看当前用户的所有订单
位置：OrderController.java 第 50 行

请求地址：
  GET /api/order/list?page=1&size=10

返回结果：
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "20260514429218",
        "userId": 1,
        "totalAmount": 28193.00,
        "status": 0,                     ← 0=待支付
        "createTime": "2026-05-14T15:27:21",
        "updateTime": "2026-05-14T15:27:21"
      }
    ],
    "total": 1,
    "pages": 1
  }
}

订单状态说明：
  0 = 待支付     ← 刚下单，还没付款（目前所有订单都在这个状态）
  1 = 已支付     ← 用户付了钱
  2 = 已发货     ← 商家发货了
  3 = 已完成     ← 订单完结

注意：
  - 只查当前登录用户的订单（通过 Token 里的 userId 过滤）
  - 最新订单排在最前面
  - 返回的是 order_item 里是没返回的，但数据是有的
```

---

## 五、Authorize 按钮（全局 Token 设置）

### 位置
页面右上角，有个 **Authorize** 按钮。

### 作用
把 Token 填在这里之后，调**所有**需要登录的接口时，都会自动带上 Token。

不用每个接口手动填写 Authorization 请求头了。

### 操作步骤

```
① 先调登录接口（POST /api/user/login）
   → 拿到返回的 token 值

② 点页面右上角 "Authorize"
   → 弹出一个输入框

③ 在 Value 栏输入（必须带 Bearer 前缀）：
   Bearer eyJhbGciOiJIUzI1NiIs...（粘贴你拿到的完整 Token）

④ 点 "Authorize" → 点 "Close"
```

### 注意
Token 有效期 24 小时。过期后需要重新登录获取新 Token，重新填。

---

## 六、如何调试一个接口（以商品列表为例）

```
完整的调试步骤：

第1步：点左边 "product-controller" 展开
第2步：点 "GET /api/product/list"
第3步：点右边的 "调试" 标签
第4步：在 Parameters 或请求头区域，会自动带入 Authorize 的 Token
第5步：在 URL 参数栏填入（如果没自动填入）：
       page = 1
       size = 5
第6步：点蓝色 "发送" 按钮
第7步：看下面的返回结果
```

---

## 七、HTTP 方法说明（GET vs POST）

页面上每个接口前面有个小标签，告诉你是 GET 还是 POST：

| 方法 | 含义 | 什么时候用 | 示例 |
|------|------|-----------|------|
| **GET** | 获取数据 | 只查不改 | 商品列表、商品详情、查购物车 |
| **POST** | 提交数据 | 新增或修改 | 注册、登录、添加购物车、下单 |

**一个记忆技巧：**
- GET = 问问题（只读不写）
- POST = 交东西（会改变数据）

---

## 八、URL 结构解析

以 `/api/product/list?page=1&size=5` 为例：

```
http://localhost:8080/api/product/list?page=1&size=5
│       │              │    │    │      │
│       │              │    │    │      └── 参数：每页5条
│       │              │    │    └───────── 参数：第1页
│       │              │    └────────────── 功能：列表
│       │              └─────────────────── 模块：商品
│       └────────────────────────────────── 所有API前缀
└────────────────────────────────────────── 本机地址+端口
```

URL 的固定结构：
```
/ + api + / + 模块名 + / + 功能 + ? + 参数
```

---

## 九、返回结果格式说明

所有接口的返回格式都是统一的：

```json
{
  "code": 200,        ← 状态码（重中之重）
  "msg": "成功",      ← 提示信息
  "data": { ... }     ← 真正的数据
}
```

### code 状态码含义

| code | 含义 | 怎么办 |
|------|------|--------|
| 200 | 成功 | 一切正常 |
| 400 | 参数错误 / 业务错误 | 看 msg 字段提示了什么 |
| 401 | 未登录 / Token 无效 | 重新登录获取新 Token |
| 500 | 服务器内部错误 | 联系后端看日志 |

### 常见的 400 错误

```
{"code":400,"msg":"用户名已存在"}
  → 注册时用了已有的用户名，换一个

{"code":400,"msg":"用户名或密码错误"}
  → 登录时账号或密码不对

{"code":400,"msg":"商品库存不足"}
  → 下单时库存不够了

{"code":400,"msg":"购物车是空的"}
  → 购物车没有商品就下单
```

---

## 十、初学者常见操作场景

### 场景1：完整的购物流程

```
第1步：注册账号
  POST /api/user/register
  {"username":"test1","password":"123456"}

第2步：登录获取 Token
  POST /api/user/login
  → 复制返回的 token，填入 Authorize

第3步：查看商品
  GET /api/product/list?page=1&size=10

第4步：加购物车
  POST /api/cart/add
  {"productId":1,"quantity":2}

第5步：查看购物车
  GET /api/cart/list

第6步：下单
  POST /api/order/create

第7步：查看订单
  GET /api/order/list
```

### 场景2：Token 过期了怎么办

```
现象：调接口返回 code=401
解决：
  ① POST /api/user/login 重新登录
  ② 拿到新 Token
  ③ 点 Authorize → 更新 Token
```

---

## 十一、这个页面不能做什么

```
✅ 这个页面能做的：
   ✓ 查看所有接口
   ✓ 填写参数调试接口
   ✓ 查看返回结果
   ✓ 管理全局 Token

❌ 这个页面不能做的：
   ✗ 显示漂亮的商城页面（需要前端 HTML/CSS）
   ✗ 管理数据库表结构
   ✗ 查看服务器日志
   ✗ 重启服务器
```

---

## 十二、最后：这个页面和你代码的关系

```
API 文档上的内容
      │
      │ 来源于
      ▼
你项目 Java 代码里的注解

例如：
─────────────────────────────────
文档上：POST /api/user/register
─────────────────────────────────
代码里 UserController.java：
  @RestController
  @RequestMapping("/api/user")         ← 决定了 /api/user
  public class UserController {
    
    @PostMapping("/register")          ← 决定了 /register
    public Result<Void> register(
      @Valid @RequestBody RegisterRequest request  ← 决定了参数
    )
  }
─────────────────────────────────

所以：
  改代码里的注解 → API 文档自动跟着变
  不需要手动维护文档
  这就是 Knife4j / Swagger 的"自动生成文档"功能
```
