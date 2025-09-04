
DROP TABLE IF EXISTS `bt_api_schema`;
CREATE TABLE `bt_api_schema` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` BIGINT(20) DEFAULT NULL COMMENT 'projectId',
  `def_key` VARCHAR(100) NULL COMMENT '标识',
  `type` varchar(20) NOT NULL DEFAULT '' COMMENT '类型',
  `title` VARCHAR(150) NULL COMMENT '名称',
  `properties` LONGTEXT DEFAULT NULL COMMENT '参数',
  `required` TEXT DEFAULT NULL COMMENT '必传参数',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) NULL COMMENT '创建人',
  `update_at` DATETIME NULL COMMENT '更新时间',
  `update_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_as_pid_key`(`project_id`,`def_key`),
  KEY `idx_as_project_id`(`project_id`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT '接口对象定义信息表';