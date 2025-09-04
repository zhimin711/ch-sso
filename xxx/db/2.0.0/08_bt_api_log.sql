CREATE TABLE `api_operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT,
  `username` VARCHAR(64),
  `operation_type` VARCHAR(64),
  `operation_content` TEXT,
  `ip` VARCHAR(32),
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `module` VARCHAR(64) COMMENT '操作模块，如项目、分组、接口、访客'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;