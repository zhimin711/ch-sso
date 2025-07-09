DROP TABLE IF EXISTS `bt_api_project`;
CREATE TABLE `bt_api_project` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` BIGINT(20) DEFAULT NULL COMMENT '项目ID',
  `workspace_id` BIGINT(20) DEFAULT NULL COMMENT '空间ID',
  `name` VARCHAR(50) NULL COMMENT '别名',
  `api_doc_url` VARCHAR(150) NULL COMMENT 'api doc url',
  `api_doc_type` VARCHAR(50) NULL COMMENT 'api doc type',
  `base_path` VARCHAR(150) NULL COMMENT '全局请求前缀',
  `icon` VARCHAR(50) NULL COMMENT '图标',
  `is_open` TINYINT(1) NULL COMMENT '是否开放',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `env` text DEFAULT NULL COMMENT '环境配置JSON格式',
  `global_config` text DEFAULT NULL COMMENT '环境配置JSON格式',
  `STATUS` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0.失效 1.生效',
  `deleted` TINYINT(1) NULL COMMENT '是否删除',
  `CREATE_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `CREATE_BY` VARCHAR(64) NULL COMMENT '创建人',
  `UPDATE_AT` DATETIME NULL COMMENT '更新时间',
  `UPDATE_BY` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_ap_project_id`(`project_id`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT '业务表-接口项目配置';

