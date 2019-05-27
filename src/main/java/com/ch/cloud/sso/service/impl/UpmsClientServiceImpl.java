package com.ch.cloud.sso.service.impl;

import com.ch.cloud.sso.service.UpmsClientService;
import com.ch.e.PubError;
import com.ch.result.Result;
import org.springframework.stereotype.Service;

/**
 * desc:
 *
 * @author zhimin
 * @date 2019/4/15 12:44 PM
 */
@Service
public class UpmsClientServiceImpl implements UpmsClientService {

    @Override
    public Result<?> findUserByUsername(String username) {
        return Result.error(PubError.CONNECT, "调用user接口失败!");
    }

    @Override
    public Result<?> findRoleByUserId(Long id) {
        return Result.error(PubError.CONNECT, "调用user接口失败!");
    }

    @Override
    public Result<?> findPermissionByUserId(Long userId) {
        return Result.error(PubError.CONNECT, "调用user接口失败!");
    }
}
