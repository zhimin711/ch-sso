package com.ch.cloud.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.cloud.api.domain.ApiTenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户环境配置Mapper接口
 *
 * @author zhimin.ma
 * @since 2024-01-01
 */
@Mapper
public interface ApiTenantMapper extends BaseMapper<ApiTenant> {

} 