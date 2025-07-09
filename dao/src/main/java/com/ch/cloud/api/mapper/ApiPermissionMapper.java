package com.ch.cloud.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.cloud.api.domain.ApiPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口权限数据服务
 */
@Mapper
public interface ApiPermissionMapper extends BaseMapper<ApiPermission> {
} 