-- Upgrade existing databases that were created before the wallet field existed.
ALTER TABLE `user`
ADD COLUMN balance DECIMAL(10,2) NOT NULL DEFAULT 10000.00 COMMENT '账户余额' AFTER credit_score;
