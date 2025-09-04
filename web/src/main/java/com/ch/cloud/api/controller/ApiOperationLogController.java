package com.ch.cloud.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ch.cloud.api.domain.ApiOperationLog;
import com.ch.cloud.api.service.IApiOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation-log")
public class ApiOperationLogController {

    @Autowired
    private IApiOperationLogService apiOperationLogService;

    @GetMapping("/page")
    public IPage<ApiOperationLog> page(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String module,
                                       @RequestParam(required = false) String username) {
        Page<ApiOperationLog> page = new Page<>(pageNum, pageSize);
        return apiOperationLogService.lambdaQuery()
                .like(module != null, ApiOperationLog::getModule, module)
                .like(username != null, ApiOperationLog::getUsername, username)
                .orderByDesc(ApiOperationLog::getCreatedTime)
                .page(page);
    }
} 