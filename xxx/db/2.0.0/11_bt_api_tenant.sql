-- 更新ApiTenant表结构
-- 将原来的环境配置字段简化为只保存环境列表

-- 1. 备份原表数据
CREATE TABLE bt_api_tenant_backup AS SELECT * FROM bt_api_tenant;

-- 2. 删除原表
DROP TABLE IF EXISTS bt_api_tenant;

-- 3. 创建新表结构
CREATE TABLE `bt_api_tenant` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `tenant_id` bigint(20) NOT NULL COMMENT '租户空间ID',
                                 `name` varchar(100) NOT NULL COMMENT '租户名称',
                                 `description` varchar(500) DEFAULT NULL COMMENT '租户描述',
                                 `env` json DEFAULT NULL COMMENT '环境配置JSON格式',
                                 `status` varchar(10) DEFAULT '1' COMMENT '状态：0.失效 1.生效',
                                 `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                                 `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
                                 `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户环境配置表';

-- 4. 数据迁移（如果有原数据）
-- 将原来的环境配置数据转换为新的JSON格式
INSERT INTO bt_api_tenant (
    workspace_id,
    name,
    description,
    env,
    status,
    deleted,
    create_at,
    create_by,
    update_at,
    update_by
)
SELECT
    workspace_id,
    COALESCE(name, CONCAT('租户', workspace_id)) as name,
    COALESCE(description, '租户环境配置') as description,
    JSON_ARRAY(
            JSON_OBJECT(
                    'envKey', COALESCE(env_key, 'dev'),
                    'name', COALESCE(name, '开发环境'),
                    'domain', COALESCE(domain, 'https://dev-api.example.com'),
                    'prefix', COALESCE(prefix, '/api/v1'),
                    'description', COALESCE(description, '开发环境配置'),
                    'isDefault', COALESCE(is_default, true)
            )
    ) as env,
    COALESCE(status, '1') as status,
    COALESCE(deleted, 0) as deleted,
    COALESCE(create_at, NOW()) as create_at,
    create_by,
    COALESCE(update_at, NOW()) as update_at,
    update_by
FROM bt_api_tenant_backup
WHERE deleted = 0;

-- 5. 删除备份表（可选，建议保留一段时间）
-- DROP TABLE bt_api_tenant_backup;