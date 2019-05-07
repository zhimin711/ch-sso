package com.ch.cloud.sso.service.impl;

import com.ch.cloud.sso.service.AdminClientService;
import com.ch.e.CoreError;
import com.ch.result.Result;
import org.springframework.stereotype.Service;

/**
 * desc:
 *
 * @author zhimin
 * @date 2019/4/15 12:44 PM
 */
@Service
public class AdminClientServiceImpl implements AdminClientService {

    @Override
    public Result<?> findUserByUsername(String username) {
        return new Result<>(CoreError.CONNECT, "调用user接口失败!");
    }

    @Override
    public Result<?> findRoleByUserId(Long id) {
        return new Result<>(CoreError.CONNECT, "调用user接口失败!");
    }

    @Override
    public Result<?> findPermissionByUserId(Long userId) {
        return new Result<>(CoreError.CONNECT, "调用user接口失败!");
    }
}
