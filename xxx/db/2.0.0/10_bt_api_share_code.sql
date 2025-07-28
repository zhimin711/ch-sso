CREATE TABLE `api_share_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT(20) DEFAULT NULL COMMENT '项目ID',
  `share_code` varchar(255) NOT NULL COMMENT '分享码',
  `resources` json DEFAULT NULL COMMENT '资源信息JSON',
  `user_id` varchar(255) NOT NULL COMMENT '用户ID',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_share_code` (`share_code`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`)
) COMMENT='API分享码表';
