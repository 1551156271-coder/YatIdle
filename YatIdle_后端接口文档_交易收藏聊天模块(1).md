# YatIdle 后端接口文档：交易、收藏、聊天模块

## 1. 文档说明

本文档整理后端 C 负责模块的接口设计，主要包括：

1. 交易订单模块 Order
2. 商品收藏模块 Favorite
3. 聊天会话与消息模块 Chat

当前版本为 MVP 阶段，暂未接入完整登录鉴权，因此接口中的当前用户 ID 暂时通过 `userId` 请求参数传入。后续接入登录模块后，应统一从登录上下文或 Token 中获取当前用户 ID。

相关核心数据表包括：

- `trade_order`：交易订单主表
- `trade_order_log`：订单状态变更日志表
- `favorite`：商品收藏表
- `chat_session`：聊天会话表
- `chat_message`：聊天消息表

---

## 2. 通用说明

### 2.1 基础路径

```text
http://localhost:8080
```

### 2.2 统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 2.3 当前用户参数

当前阶段临时使用：

```text
?userId=1
```

示例：

```text
GET /api/favorites?userId=1
```

后续登录鉴权完成后，`userId` 不再由前端传入。

---

# 一、交易订单模块 Order

## 1. 创建订单

### 接口说明

买家对某个商品创建交易订单。

### 请求方式

```text
POST
```

### 请求路径

```text
/api/orders
```

### 请求参数

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前买家用户 ID，临时测试用 |

Body 参数：

```json
{
  "itemId": 1,
  "tradeLocation": "东校园教学楼门口",
  "remark": "下午五点后方便交易"
}
```

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `itemId` | Long | 是 | 商品 ID |
| `tradeLocation` | String | 否 | 线下交易地点 |
| `remark` | String | 否 | 订单备注 |

### 请求示例

```text
POST http://localhost:8080/api/orders?userId=1
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "YI202605151030001234",
    "itemId": 1,
    "buyerId": 1,
    "sellerId": 2,
    "price": 25.00,
    "status": "PENDING",
    "tradeLocation": "东校园教学楼门口",
    "remark": "下午五点后方便交易",
    "createTime": "2026-05-15T10:30:00"
  }
}
```

### 业务规则

1. 商品必须存在；
2. 商品不能被逻辑删除；
3. 商品状态应为 `ON_SALE`；
4. 买家不能购买自己发布的商品；
5. 创建订单后，订单状态为 `PENDING`；
6. 创建订单时写入 `trade_order_log`，`action` 为 `CREATE`。

### 可能异常

```text
商品ID不能为空
商品不存在
商品当前不可交易
不能购买自己发布的商品
该商品已有待交易订单
```

---

## 2. 查询我买到的订单

### 接口说明

查询当前用户作为买家的订单列表。

### 请求方式

```text
GET
```

### 请求路径

```text
/api/orders/my-buy
```

### 请求参数

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
GET http://localhost:8080/api/orders/my-buy?userId=1
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "orderNo": "YI202605151030001234",
      "itemId": 1,
      "buyerId": 1,
      "sellerId": 2,
      "price": 25.00,
      "status": "PENDING",
      "tradeLocation": "东校园教学楼门口",
      "remark": "下午五点后方便交易",
      "createTime": "2026-05-15T10:30:00"
    }
  ]
}
```

---

## 3. 查询我卖出的订单

### 接口说明

查询当前用户作为卖家的订单列表。

### 请求方式

```text
GET
```

### 请求路径

```text
/api/orders/my-sell
```

### 请求参数

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
GET http://localhost:8080/api/orders/my-sell?userId=2
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "orderNo": "YI202605151030001234",
      "itemId": 1,
      "buyerId": 1,
      "sellerId": 2,
      "price": 25.00,
      "status": "PENDING",
      "tradeLocation": "东校园教学楼门口",
      "remark": "下午五点后方便交易",
      "createTime": "2026-05-15T10:30:00"
    }
  ]
}
```

---

## 4. 取消订单

### 接口说明

买家或卖家取消处于 `PENDING` 状态的订单。

### 请求方式

```text
PUT
```

### 请求路径

```text
/api/orders/{id}/cancel
```

### 请求参数

Path 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 订单 ID |

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前操作用户 ID |

Body 参数：

```json
{
  "cancelReason": "双方协商取消"
}
```

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `cancelReason` | String | 否 | 取消原因 |

### 请求示例

```text
PUT http://localhost:8080/api/orders/1/cancel?userId=1
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "YI202605151030001234",
    "itemId": 1,
    "buyerId": 1,
    "sellerId": 2,
    "price": 25.00,
    "status": "CANCELLED",
    "tradeLocation": "东校园教学楼门口",
    "remark": "下午五点后方便交易",
    "cancelReason": "双方协商取消",
    "cancelTime": "2026-05-15T11:00:00"
  }
}
```

### 业务规则

1. 订单必须存在；
2. 订单不能被逻辑删除；
3. 只有 `PENDING` 状态订单可以取消；
4. 只有订单买家或卖家可以取消订单；
5. 取消后订单状态变为 `CANCELLED`；
6. 取消时写入 `trade_order_log`，`action` 为 `CANCEL`。

### 可能异常

```text
订单ID不能为空
订单不存在
当前订单状态不能取消
无权取消该订单
```

---

## 5. 完成订单

### 接口说明

买家或卖家确认交易完成。

### 请求方式

```text
PUT
```

### 请求路径

```text
/api/orders/{id}/complete
```

### 请求参数

Path 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 订单 ID |

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前操作用户 ID |

### 请求示例

```text
PUT http://localhost:8080/api/orders/1/complete?userId=1
```

### Body

```text
none
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "YI202605151030001234",
    "itemId": 1,
    "buyerId": 1,
    "sellerId": 2,
    "price": 25.00,
    "status": "COMPLETED",
    "completeTime": "2026-05-15T11:30:00"
  }
}
```

### 业务规则

1. 订单必须存在；
2. 订单不能被逻辑删除；
3. 只有 `PENDING` 状态订单可以完成；
4. 只有订单买家或卖家可以完成订单；
5. 完成后订单状态变为 `COMPLETED`；
6. 完成后对应商品状态变为 `SOLD`；
7. 完成时写入 `trade_order_log`，`action` 为 `COMPLETE`。

### 可能异常

```text
订单ID不能为空
订单不存在
当前订单状态不能完成
无权完成该订单
商品不存在
```

---

# 二、商品收藏模块 Favorite

## 1. 收藏商品

### 接口说明

用户收藏指定商品。

### 请求方式

```text
POST
```

### 请求路径

```text
/api/favorites/{itemId}
```

### 请求参数

Path 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `itemId` | Long | 是 | 商品 ID |

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
POST http://localhost:8080/api/favorites/1?userId=1
```

### Body

```text
none
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 业务规则

1. 商品必须存在；
2. 商品不能被逻辑删除；
3. 同一用户不能重复收藏同一商品；
4. 收藏成功后在 `favorite` 表中新增记录。

### 可能异常

```text
商品ID不能为空
商品不存在
已收藏该商品
```

---

## 2. 取消收藏

### 接口说明

用户取消收藏指定商品。

### 请求方式

```text
DELETE
```

### 请求路径

```text
/api/favorites/{itemId}
```

### 请求参数

Path 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `itemId` | Long | 是 | 商品 ID |

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
DELETE http://localhost:8080/api/favorites/1?userId=1
```

### Body

```text
none
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 业务规则

1. 根据 `userId + itemId` 删除收藏记录；
2. 若用户原本未收藏该商品，也可以正常返回成功；
3. 这里的 `itemId` 是商品 ID，不是 `favorite` 表主键 ID。

---

## 3. 查询我的收藏列表

### 接口说明

查询当前用户收藏的商品列表。

### 请求方式

```text
GET
```

### 请求路径

```text
/api/favorites
```

### 请求参数

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
GET http://localhost:8080/api/favorites?userId=1
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "itemId": 1,
      "itemTitle": "二手计算器",
      "price": 25.00,
      "itemStatus": "ON_SALE",
      "favoriteTime": "2026-05-15T14:20:00"
    }
  ]
}
```

### 业务规则

1. 根据 `userId` 查询 `favorite` 表；
2. 根据 `favorite.item_id` 查询商品信息；
3. 返回商品标题、价格、状态、收藏时间等信息；
4. 已被逻辑删除的商品不返回。

---

# 三、聊天模块 Chat

## 1. 创建或获取聊天会话

### 接口说明

买家点击“联系卖家”时，根据商品创建或获取已有聊天会话。

### 请求方式

```text
POST
```

### 请求路径

```text
/api/chat/sessions
```

### 请求参数

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID，一般为买家 ID |

Body 参数：

```json
{
  "itemId": 1
}
```

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `itemId` | Long | 是 | 商品 ID |

### 请求示例

```text
POST http://localhost:8080/api/chat/sessions?userId=1
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "itemId": 1,
    "itemTitle": "二手计算器",
    "buyerId": 1,
    "sellerId": 2,
    "lastMessage": null,
    "lastSenderId": null,
    "lastMessageTime": null,
    "unreadCount": 0
  }
}
```

### 业务规则

1. 商品必须存在；
2. 商品不能被逻辑删除；
3. 不能和自己发布的商品建立会话；
4. 同一买家、同一卖家、同一商品只能有一个会话；
5. 若会话已存在，则直接返回已有会话；
6. 若会话不存在，则新建 `chat_session`。

### 可能异常

```text
商品ID不能为空
商品不存在
不能和自己发布的商品建立会话
```

---

## 2. 查询我的聊天会话列表

### 接口说明

查询当前用户参与的所有聊天会话。

### 请求方式

```text
GET
```

### 请求路径

```text
/api/chat/sessions
```

### 请求参数

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
GET http://localhost:8080/api/chat/sessions?userId=2
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "itemId": 1,
      "itemTitle": "二手计算器",
      "buyerId": 1,
      "sellerId": 2,
      "lastMessage": "你好，请问这个商品还在吗？",
      "lastSenderId": 1,
      "lastMessageTime": "2026-05-18T16:30:00",
      "unreadCount": 1
    }
  ]
}
```

### 业务规则

1. 查询 `buyer_id = userId` 或 `seller_id = userId` 的会话；
2. 返回最近消息、最近发送人、最近消息时间；
3. `unreadCount` 返回当前用户自己的未读数；
4. 如果当前用户是买家，返回 `buyer_unread_count`；
5. 如果当前用户是卖家，返回 `seller_unread_count`。

---

## 3. 发送聊天消息

### 接口说明

用户在指定会话中发送文本消息。

### 请求方式

```text
POST
```

### 请求路径

```text
/api/chat/messages
```

### 请求参数

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前发送者用户 ID |

Body 参数：

```json
{
  "sessionId": 1,
  "content": "你好，请问这个商品还在吗？"
}
```

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `sessionId` | Long | 是 | 聊天会话 ID |
| `content` | String | 是 | 消息内容 |

### 请求示例

```text
POST http://localhost:8080/api/chat/messages?userId=1
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sessionId": 1,
    "senderId": 1,
    "receiverId": 2,
    "messageType": "TEXT",
    "content": "你好，请问这个商品还在吗？",
    "readFlag": 0,
    "createTime": "2026-05-18T16:30:00"
  }
}
```

### 业务规则

1. 会话必须存在；
2. 会话不能被逻辑删除；
3. 当前用户必须是该会话的买家或卖家；
4. 消息内容不能为空；
5. 消息写入 `chat_message`；
6. 更新 `chat_session.last_message`；
7. 更新 `chat_session.last_sender_id`；
8. 更新 `chat_session.last_message_time`；
9. 若买家发送消息，则 `seller_unread_count + 1`；
10. 若卖家发送消息，则 `buyer_unread_count + 1`。

### 可能异常

```text
会话ID不能为空
消息内容不能为空
会话不存在
无权发送该会话消息
```

---

## 4. 上传聊天图片

### 接口说明

用户发送图片消息前，先上传图片文件，后端返回图片相对访问路径。前端拿到图片路径后，再调用发送聊天消息接口，并将 `messageType` 设置为 `IMAGE`。

### 请求方式

```text
POST
```

### 请求路径

```text
/api/chat/images/upload
```

### Content-Type

```text
multipart/form-data
```

### 请求参数

Form Data 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `file` | File | 是 | 要上传的聊天图片文件 |

### 请求示例

```text
POST http://localhost:8080/api/chat/images/upload
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/uploads/chat/xxxx.jpg"
  }
}
```

### 业务规则

1. 上传文件不能为空；
2. 仅允许上传图片类型文件；
3. 图片保存到后端 `uploads/chat` 目录；
4. 返回的 `url` 为相对路径，可通过 `http://localhost:8080` 拼接访问；
5. 发送图片消息时，将返回的 `url` 填入发送消息接口的 `content` 字段，并设置 `messageType` 为 `IMAGE`。

### 可能异常

```text
图片文件不能为空
只能上传图片文件
```

---
## 5. 查询会话消息列表

### 接口说明

查询某个聊天会话下的消息记录。

### 请求方式

```text
GET
```

### 请求路径

```text
/api/chat/sessions/{sessionId}/messages
```

### 请求参数

Path 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `sessionId` | Long | 是 | 聊天会话 ID |

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
GET http://localhost:8080/api/chat/sessions/1/messages?userId=2
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "sessionId": 1,
      "senderId": 1,
      "receiverId": 2,
      "messageType": "TEXT",
      "content": "你好，请问这个商品还在吗？",
      "readFlag": 0,
      "createTime": "2026-05-18T16:30:00"
    },
    {
      "id": 2,
      "sessionId": 1,
      "senderId": 2,
      "receiverId": 1,
      "messageType": "TEXT",
      "content": "还在，可以东校园面交。",
      "readFlag": 0,
      "createTime": "2026-05-18T16:35:00"
    }
  ]
}
```

### 业务规则

1. 会话必须存在；
2. 当前用户必须是该会话的买家或卖家；
3. 只返回当前会话下未逻辑删除的消息；
4. 消息按 `create_time` 升序排列。

### 可能异常

```text
会话ID不能为空
会话不存在
无权查看该会话消息
```

---

## 6. 标记会话已读

### 接口说明

用户进入聊天会话后，清空当前用户对应的未读消息数。

### 请求方式

```text
PUT
```

### 请求路径

```text
/api/chat/sessions/{sessionId}/read
```

### 请求参数

Path 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `sessionId` | Long | 是 | 聊天会话 ID |

Query 参数：

| 参数名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `userId` | Long | 是 | 当前用户 ID |

### 请求示例

```text
PUT http://localhost:8080/api/chat/sessions/1/read?userId=2
```

### Body

```text
none
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 业务规则

1. 会话必须存在；
2. 当前用户必须是该会话的买家或卖家；
3. 如果当前用户是买家，则 `buyer_unread_count = 0`；
4. 如果当前用户是卖家，则 `seller_unread_count = 0`。

### 可能异常

```text
会话ID不能为空
会话不存在
无权操作该会话
```

---

# 四、状态与枚举说明

## 1. 订单状态 OrderStatusEnum

```java
public enum OrderStatusEnum {
    PENDING,
    COMPLETED,
    CANCELLED;
}
```

| 状态 | 说明 |
|---|---|
| `PENDING` | 待交易 |
| `COMPLETED` | 已完成 |
| `CANCELLED` | 已取消 |

---

## 2. 订单日志操作 OrderLogActionEnum

```java
public enum OrderLogActionEnum {
    CREATE,
    CANCEL,
    COMPLETE;
}
```

| 操作 | 说明 |
|---|---|
| `CREATE` | 创建订单 |
| `CANCEL` | 取消订单 |
| `COMPLETE` | 完成订单 |

---

## 3. 商品状态 ItemStatusEnum

```java
public enum ItemStatusEnum {
    ON_SALE,
    SOLD,
    REMOVED;
}
```

| 状态 | 说明 |
|---|---|
| `ON_SALE` | 在售 |
| `SOLD` | 已售出 |
| `REMOVED` | 已下架 |

---

## 4. 消息类型 MessageTypeEnum

```java
public enum MessageTypeEnum {
    TEXT,
    IMAGE;
}
```

| 类型 | 说明 |
|---|---|
| `TEXT` | 文本消息 |
| `IMAGE` | 图片消息 |

---

# 五、当前版本说明

1. 当前版本为后端 MVP 版本；
2. 当前用户 ID 暂时通过 `userId` 参数传入；
3. 后续接入登录鉴权后，`userId` 应从登录上下文中获取；
4. 聊天模块当前通过 HTTP 接口实现基础消息功能；
5. 软件体系结构设计中预留 T-io WebSocket 扩展能力，用于后续实现实时通信；
6. 当前暂未实现真实支付、物流、退款、实时推送。
