-- Upgrade existing databases after syncing wanted condition values.
-- The application now stores wanted condition values such as
-- '99新及以上', '95新及以上', '90新及以上', '85新及以上', and '80新及以上'.
ALTER TABLE wanted
MODIFY condition_level VARCHAR(50) DEFAULT NULL COMMENT '期望成色';
