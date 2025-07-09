package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiPermission;
import com.ch.cloud.api.mapper.ApiPermissionMapper;
import com.ch.cloud.api.service.IApiPermissionService;
import org.springframework.stereotype.Service;

/**
 * 接口权限Service业务层
 */
@Service
public class ApiPermissionServiceImpl extends ServiceImpl<ApiPermissionMapper, ApiPermission> implements IApiPermissionService {
} 