-- ----------------------------
-- Table structure for bt_api_tenant
-- ----------------------------
DROP TABLE IF EXISTS `bt_api_tenant`;
CREATE TABLE `bt_api_tenant` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `workspace_id` BIGINT(20) NOT NULL COMMENT '租户空间ID',
  `name` VARCHAR(100) NOT NULL COMMENT '环境配置名称',
  `env_key` VARCHAR(50) NOT NULL COMMENT '环境标识(dev/test/prod等)',
  `domain` VARCHAR(255) NOT NULL COMMENT '环境域名',
  `prefix` VARCHAR(100) DEFAULT NULL COMMENT '请求前缀',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '环境描述',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认环境',
  `env_config` JSON DEFAULT NULL COMMENT '环境配置JSON格式',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态：0.失效 1.生效',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `create_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `update_at` DATETIME DEFAULT NULL COMMENT '更新时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tec_workspace_env` (`workspace_id`, `env_key`, `deleted`),
  KEY `idx_tec_workspace_id` (`workspace_id`),
  KEY `idx_tec_env_key` (`env_key`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT='租户环境配置表'; 