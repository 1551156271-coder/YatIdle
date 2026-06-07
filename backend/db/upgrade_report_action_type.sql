ALTER TABLE report
ADD COLUMN action_type VARCHAR(50) DEFAULT NULL COMMENT '举报处理联动动作' AFTER handle_result;
