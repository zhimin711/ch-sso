
DROP TABLE IF EXISTS `bt_api_group`;
CREATE TABLE `bt_api_group` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '上级Id: 0为1级',
  `project_id` BIGINT(20) DEFAULT NULL COMMENT '项目ID',
  `code` VARCHAR(64) NULL COMMENT '代码',
  `name` VARCHAR(150) NULL COMMENT '名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `type` CHAR(1) NOT NULL DEFAULT '0' COMMENT '类型：0.未分组 1.接口分组 2.标签分组 3.自定义分组',
  `is_import` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为导入',
  `import_at` DATETIME NULL COMMENT '导入时间',
  `create_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) NULL COMMENT '创建人',
  `update_at` DATETIME NULL COMMENT '更新时间',
  `update_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_ag_project_id`(`project_id`),
  KEY `idx_ag_create_by`(`create_by`),
  KEY `idx_ag_parent_id`(`parent_id`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT '接口分组表';