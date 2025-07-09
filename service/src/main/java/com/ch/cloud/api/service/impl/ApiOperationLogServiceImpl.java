package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiOperationLog;
import com.ch.cloud.api.mapper.ApiOperationLogMapper;
import com.ch.cloud.api.service.IApiOperationLogService;
import com.ch.utils.DateUtils;
import org.springframework.stereotype.Service;

@Service
public class ApiOperationLogServiceImpl extends ServiceImpl<ApiOperationLogMapper, ApiOperationLog> implements IApiOperationLogService {
    @Override
    public void saveLog(String userId, String username, String module, String operationType, String operationContent, String ip) {
        ApiOperationLog log = new ApiOperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setIp(ip);
        log.setCreatedTime(DateUtils.current());
        this.save(log);
    }
}
