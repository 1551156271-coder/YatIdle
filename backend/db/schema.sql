CREATE DATABASE IF NOT EXISTS yat_idle DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
USE yat_idle;

CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  avatar VARCHAR(255),
  role VARCHAR(20) NOT NULL DEFAULT 'buyer',
  status VARCHAR(20) NOT NULL DEFAULT 'active',
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_user_role (role),
  INDEX idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  category_id BIGINT,
  status VARCHAR(20) NOT NULL DEFAULT 'pending',
  view_count INT NOT NULL DEFAULT 0,
  favorite_count INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_item_user_id (user_id),
  INDEX idx_item_category_id (category_id),
  INDEX idx_item_status (status),
  INDEX idx_item_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS item_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_id BIGINT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_item_image_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  pay_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'pending_payment',
  pay_time DATETIME,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_order_no (order_no),
  INDEX idx_order_buyer_id (buyer_id),
  INDEX idx_order_seller_id (seller_id),
  INDEX idx_order_item_id (item_id),
  INDEX idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  before_status VARCHAR(20),
  after_status VARCHAR(20) NOT NULL,
  operator_id BIGINT NOT NULL,
  remark VARCHAR(255),
  create_time DATETIME NOT NULL,
  INDEX idx_order_log_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  last_message VARCHAR(255),
  last_message_time DATETIME,
  unread_count INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  INDEX idx_chat_session_item_id (item_id),
  INDEX idx_chat_session_buyer_id (buyer_id),
  INDEX idx_chat_session_seller_id (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  message_type VARCHAR(20) NOT NULL DEFAULT 'text',
  content TEXT NOT NULL,
  read_flag TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  INDEX idx_chat_message_session_id (session_id),
  INDEX idx_chat_message_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
