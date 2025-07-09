-- ----------------------------
-- Table structure for 04_rt_api_version.sql
-- ----------------------------
DROP TABLE IF EXISTS `rt_api_project_user`;
CREATE TABLE `rt_api_project_user`
(
    `project_id` bigint(20) NOT NULL COMMENT 'projectId',
    `user_id`    varchar(32) NOT NULL COMMENT 'userId',
    `role`       varchar(50) NOT NULL COMMENT 'role',
    PRIMARY KEY (`project_id`, `user_id`),
    KEY          `idx_rt_av_api_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='关系表-用户项目角色表';
