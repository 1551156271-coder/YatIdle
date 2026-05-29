package com.yatidle.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdminSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public AdminSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void run() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS report (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  reporter_id BIGINT NOT NULL,
                  target_user_id BIGINT DEFAULT NULL,
                  item_id BIGINT DEFAULT NULL,
                  wanted_id BIGINT DEFAULT NULL,
                  order_id BIGINT DEFAULT NULL,
                  chat_session_id BIGINT DEFAULT NULL,
                  reason VARCHAR(50) NOT NULL,
                  description VARCHAR(500) DEFAULT NULL,
                  image_urls TEXT DEFAULT NULL,
                  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                  handle_result VARCHAR(500) DEFAULT NULL,
                  handler_id BIGINT DEFAULT NULL,
                  handle_time DATETIME DEFAULT NULL,
                  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  is_deleted TINYINT NOT NULL DEFAULT 0,
                  INDEX idx_report_status (status),
                  INDEX idx_report_reporter_id (reporter_id),
                  INDEX idx_report_target_user_id (target_user_id),
                  INDEX idx_report_create_time (create_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS admin_action_log (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  admin_id BIGINT NOT NULL,
                  action VARCHAR(50) NOT NULL,
                  target_type VARCHAR(50) NOT NULL,
                  target_id BIGINT NOT NULL,
                  before_status VARCHAR(50) DEFAULT NULL,
                  after_status VARCHAR(50) DEFAULT NULL,
                  remark VARCHAR(500) DEFAULT NULL,
                  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  INDEX idx_admin_log_admin_id (admin_id),
                  INDEX idx_admin_log_target (target_type, target_id),
                  INDEX idx_admin_log_create_time (create_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
    }
}
