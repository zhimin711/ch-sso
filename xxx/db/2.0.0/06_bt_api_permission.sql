
DROP TABLE IF EXISTS `bt_api_permission`;
CREATE TABLE `bt_api_permission` (
                                     `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `parent_id` BIGINT(20) DEFAULT 0 COMMENT '上级ID',
                                     `code` VARCHAR(50) NULL COMMENT '代码',
                                     `name` VARCHAR(50) NULL COMMENT '名称',
                                     `desc` VARCHAR(255) DEFAULT NULL COMMENT '描述',
                                     `type` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0. 1.菜单 2.按钮',
                                     roles varchar(100) NULL COMMENT '角色列表';
`create_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) NULL COMMENT '创建人',
  `update_at` DATETIME NULL COMMENT '更新时间',
  `update_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  unique KEY `uk_ap_status`(`code`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT '接口权限表';

INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(1, 0, 'workbench', '工作台', NULL, '1', '2025-06-20 16:11:47', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(2, 0, 'settings', '项目配置', NULL, '1', '2025-06-20 16:12:19', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(3, 0, 'api', '项目接口', NULL, '1', '2025-06-20 16:12:37', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(4, 0, 'model', '接口模型', NULL, '1', '2025-06-20 16:13:41', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(5, 0, 'visitor', '访客管理', NULL, '1', '2025-06-20 16:14:08', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(6, 1, 'statistics', '统计-工作台数据', NULL, '2', '2025-06-20 16:40:40', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(7, 1, 'activities', '查询-项目动态', NULL, '2', '2025-06-20 16:41:19', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(8, 1, 'recent-apis', '查询-最近更新接口', NULL, '2', '2025-06-20 16:41:45', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(9, 2, 'project-info', '查询-项目信息', NULL, '2', '2025-06-20 16:42:32', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(10, 2, 'project-members', '查询-项目成员', NULL, '2', '2025-06-20 16:43:31', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(11, 2, 'project-settings', '查询-项目配置', NULL, '2', '2025-06-20 17:20:40', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(12, 2, 'project-settings-save', '保存-项目配置', NULL, '2', '2025-06-20 17:20:40', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(13, 3, 'group-tree', '查询-接口分组树', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(14, 3, 'group-add', '添加-接口分组', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(15, 3, 'group-edit', '编辑-接口分组', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(16, 3, 'group-del', '删除-接口分组', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(17, 3, 'path-add', '添加-接口信息', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(18, 3, 'path-edit', '编辑-接口信息', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(19, 3, 'path-del', '删除-接口信息', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(20, 3, 'path-copy', '复制-接口信息', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(21, 3, 'path-get', '查询-接口信息', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(22, 2, 'project-api-import', '导入-项目接口', NULL, '2', '2025-06-20 17:20:40', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(23, 5, 'visitor-add', '添加-访客', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(24, 5, 'visitor-list', '查询-访客', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(25, 5, 'visitor-del', '删除-访客', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);
INSERT INTO ch_devops.bt_api_permission
(id, parent_id, code, name, `desc`, `type`, create_at, create_by, update_at, update_by, roles)
VALUES(26, 5, 'visitor-members', '查询-可添加访客列表', NULL, '2', '2025-06-20 17:24:48', NULL, NULL, NULL, NULL);