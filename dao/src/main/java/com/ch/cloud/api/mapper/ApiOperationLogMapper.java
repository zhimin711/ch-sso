package com.ch.cloud.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.cloud.api.domain.ApiOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiOperationLogMapper extends BaseMapper<ApiOperationLog> {
} 