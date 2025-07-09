package com.ch.cloud.api.service.impl;

import com.ch.cloud.api.mapper2.IApiProjectUserMapper;
import com.ch.cloud.api.service.IApiProjectUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiProjectUserServiceImpl implements IApiProjectUserService {
    @Autowired
    private IApiProjectUserMapper apiProjectUserMapper;

    @Override
    public boolean addVisitor(Long projectId, String userId) {
        int exists = apiProjectUserMapper.exists(projectId, userId);
        if (exists > 0) {
            return false;
        }
        return apiProjectUserMapper.insertVisitor(projectId, userId, "VISITOR") > 0;
    }

    @Override
    public boolean removeVisitor(Long projectId, String userId) {
        return apiProjectUserMapper.deleteVisitor(projectId, userId, "VISITOR") > 0;
    }

    @Override
    public List<String> findVisitorUserIds(Long projectId) {
        return apiProjectUserMapper.findVisitorUserIds(projectId, "VISITOR");
    }

    @Override
    public int exists(Long projectId, String userId) {
        return apiProjectUserMapper.exists(projectId, userId);
    }
} 