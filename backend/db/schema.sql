CREATE DATABASE IF NOT EXISTS yatidle DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
USE yatidle;

CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一登录标识',
  password VARCHAR(100) NOT NULL COMMENT '用户密码',
  phone VARCHAR(20) COMMENT '手机号',
  avatar VARCHAR(255) COMMENT '头像URL',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '用户状态',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记，0未删除，1已删除',
  INDEX idx_user_role (role),
  INDEX idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
  user_id BIGINT NOT NULL COMMENT '发布用户ID',
  title VARCHAR(100) NOT NULL COMMENT '商品标题',
  campus enum('南校园', '东校园', '北校园', '珠海校区', '深圳校区') default null COMMENT '商品所在校区',
  condition_level enum('全新', '99新', '95新', '9成新', '八成新', '八成新以下') default null COMMENT '商品成色',
  description TEXT COMMENT '商品描述',
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品价格',
  category_id BIGINT COMMENT '分类ID',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '商品状态',
  view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记，0未删除，1已删除',
  INDEX idx_item_user_id (user_id),
  INDEX idx_item_category_id (category_id),
  INDEX idx_item_status (status),
  INDEX idx_item_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS item_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品图片ID',
  item_id BIGINT NOT NULL COMMENT '商品ID',
  image_url VARCHAR(255) NOT NULL COMMENT '图片URL',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '图片排序值，越小越靠前',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记，0未删除，1已删除',
  INDEX idx_item_image_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

CREATE TABLE IF NOT EXISTS `order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单编号',
  buyer_id BIGINT NOT NULL COMMENT '买家用户ID',
  seller_id BIGINT NOT NULL COMMENT '卖家用户ID',
  item_id BIGINT NOT NULL COMMENT '商品ID',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  pay_amount DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
  status VARCHAR(20) NOT NULL DEFAULT 'pending_payment' COMMENT '订单状态',
  pay_time DATETIME COMMENT '支付时间',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  cancel_time DATETIME COMMENT '取消时间',
  complete_time DATETIME COMMENT '完成时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记，0未删除，1已删除',
  INDEX idx_order_no (order_no),
  INDEX idx_order_buyer_id (buyer_id),
  INDEX idx_order_seller_id (seller_id),
  INDEX idx_order_item_id (item_id),
  INDEX idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS order_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单日志ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  before_status VARCHAR(20) COMMENT '变更前订单状态',
  after_status VARCHAR(20) NOT NULL COMMENT '变更后订单状态',
  operator_id BIGINT NOT NULL COMMENT '操作用户ID',
  remark VARCHAR(255) COMMENT '操作备注',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  INDEX idx_order_log_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志表';

CREATE TABLE IF NOT EXISTS chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '聊天会话ID',
  item_id BIGINT NOT NULL COMMENT '关联商品ID',
  buyer_id BIGINT NOT NULL COMMENT '买家用户ID',
  seller_id BIGINT NOT NULL COMMENT '卖家用户ID',
  last_message VARCHAR(255) COMMENT '最近一条消息摘要',
  last_message_time DATETIME COMMENT '最近消息时间',
  unread_count INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  INDEX idx_chat_session_item_id (item_id),
  INDEX idx_chat_session_buyer_id (buyer_id),
  INDEX idx_chat_session_seller_id (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '聊天消息ID',
  session_id BIGINT NOT NULL COMMENT '聊天会话ID',
  sender_id BIGINT NOT NULL COMMENT '发送者用户ID',
  receiver_id BIGINT NOT NULL COMMENT '接收者用户ID',
  message_type VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '消息类型',
  content TEXT NOT NULL COMMENT '消息内容',
  read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '已读标记，0未读，1已读',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  INDEX idx_chat_message_session_id (session_id),
  INDEX idx_chat_message_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
