
DROP TABLE IF EXISTS `bt_api_path`;
CREATE TABLE `bt_api_path` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` BIGINT(20) DEFAULT NULL COMMENT 'projectId',
  `name` VARCHAR(150) NULL COMMENT '接口名称',
  `path` VARCHAR(150) NULL COMMENT '接口地址',
  `method` VARCHAR(10) NULL COMMENT '请求方法',
  `is_wrap` tinyint(1) NULL comment '是否封装',
  `consumes` tinytext NULL comment '请求头中 Content-Type 值',
  `produces` tinytext NULL comment '请求头中 Accept 值',
  `parameters` text NULL comment '请求参数:json',
  `body` text NULL comment '请求内容:json',
  `responses` text NULL comment '返回参数:json',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0. 1. 2.',
  `is_import` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为导入',
  `import_content` text NULL comment '导入内容:json',
  `import_at` DATETIME NULL COMMENT '导入时间',
  `create_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) NULL COMMENT '创建人',
  `update_at` DATETIME NULL COMMENT '更新时间',
  `update_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  unique KEY `uk_ap_path_method`(`path`,`method`,`project_id`),
  KEY `idx_ap_project_id`(`project_id`),
  KEY `idx_ap_status`(`status`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT '接口分组表';