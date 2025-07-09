package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiPath;
import com.ch.result.InvokerPage;

import java.util.List;

/**
 * 接口分组业务服务
 * 
 * @author zhimin.ma
 * @since 2022-10-16 11:35:57
 */
public interface IApiPathService extends IService<ApiPath> {
    InvokerPage.Page<ApiPath> pageByGroupId(Long groupId, int pageNum, int pageSize);

    List<Long> listGroupIds(Long pathId);
}
