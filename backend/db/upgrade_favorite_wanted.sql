-- Support favorites for both sale items and wanted posts.
-- Safe to run more than once on MySQL 8.x.

SET @schema_name = DATABASE();

SET @has_wanted_id = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'favorite'
    AND COLUMN_NAME = 'wanted_id'
);

SET @sql = IF(
  @has_wanted_id = 0,
  'ALTER TABLE favorite ADD COLUMN wanted_id BIGINT NULL COMMENT ''求购ID（收藏求购时使用）'' AFTER item_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE favorite MODIFY COLUMN item_id BIGINT NULL COMMENT '商品ID（收藏普通商品时使用）';

SET @has_wanted_index = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'favorite'
    AND INDEX_NAME = 'idx_favorite_wanted_id'
);

SET @sql = IF(
  @has_wanted_index = 0,
  'ALTER TABLE favorite ADD INDEX idx_favorite_wanted_id (wanted_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
