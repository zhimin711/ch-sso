package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiOperationLog;

public interface IApiOperationLogService extends IService<ApiOperationLog> {
    void saveLog(String userId, String username, String module, String operationType, String operationContent, String ip);
} 