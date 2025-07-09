package com.ch.cloud.api.service;

import java.util.List;

public interface IApiProjectUserService {
    /**
     * 添加访客
     */
    boolean addVisitor(Long projectId, String userId);

    /**
     * 移除访客
     */
    boolean removeVisitor(Long projectId, String userId);

    /**
     * 查询项目所有访客userId
     */
    List<String> findVisitorUserIds(Long projectId);

    int exists(Long projectId, String userId);
}