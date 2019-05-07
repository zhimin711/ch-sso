
CREATE TABLE `oauth_client_details` (
  `client_id` varchar(256) NOT NULL COMMENT '',
  `resource_ids` varchar(256) DEFAULT NULL COMMENT '',
  `client_secret` varchar(256) DEFAULT NULL COMMENT '',
  `scope` varchar(256) DEFAULT NULL COMMENT '',
  `authorized_grant_types` varchar(256) DEFAULT NULL COMMENT '验证方式: authorization_code授权码',
  `web_server_redirect_uri` varchar(256) DEFAULT NULL COMMENT '',
  `authorities` varchar(256) DEFAULT NULL COMMENT '',
  `access_token_validity` int(11) DEFAULT NULL COMMENT '',
  `refresh_token_validity` int(11) DEFAULT NULL COMMENT '',
  `additional_information` varchar(4096) DEFAULT NULL COMMENT '',
  `autoapprove` varchar(256) DEFAULT NULL  COMMENT '是否自动认证',
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB;

INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES ('ChaoHua', 'auth-server', '$2a$10$AO8Oy7gUn0XFVPwJiCoSTuOOCmPinDiJeSpF73sYWqpCT0wuMDRX2', 'user_info', 'authorization_code', 'http://localhost:8083/blog/login', NULL, '10000', '1000', NULL, 'true1');
INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES ('MemberSystem', NULL, '$2a$10$dYRcFip80f0jIKGzRGulFelK12036xWQKgajanfxT65QB4htsEXNK', 'user_info', 'authorization_code,password,refresh_token', 'http://localhost:8081/login', NULL, NULL, NULL, NULL, 'user_info');
