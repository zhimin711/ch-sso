-- ----------------------------
-- Table structure for 05_rt_api_group_path.sql
-- ----------------------------
DROP TABLE IF EXISTS `rt_api_group_path`;
CREATE TABLE `rt_api_group_path`(
  `group_id` bigint(20) NOT NULL COMMENT '分组ID',
  `path_id` bigint(20) NOT NULL COMMENT '接口ID',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY(`group_id`,`path_id`),
  KEY `idx_rt_ag_path_id` (`path_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='关系表-接口分组';
