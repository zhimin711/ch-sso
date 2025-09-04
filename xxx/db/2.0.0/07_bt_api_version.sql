/**
 * st: 系统表(system_table)
 * mt: 基础表(master_table)
 * bt: 业务表(business_table)
 * rt: 关系表(relation_table)
 * it: 接口表(interface_table)
 * lt: 日志表(log_table)
 */

DROP TABLE IF EXISTS `bt_api_version`;
CREATE TABLE `bt_api_version` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` BIGINT(20) DEFAULT NULL COMMENT 'projectId',
  `name` VARCHAR(50) NULL COMMENT '名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0. 1. 2.',
  `create_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) NULL COMMENT '创建人',
  `update_at` DATETIME NULL COMMENT '更新时间',
  `update_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_av_status`(`status`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT '接口分组表';