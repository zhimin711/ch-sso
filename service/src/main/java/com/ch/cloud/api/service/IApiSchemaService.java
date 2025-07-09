package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiSchema;

/**
 * 接口对象定义信息业务服务
 * 
 * @author zhimin.ma
 * @since 2022-10-16 18:03:21
 */
public interface IApiSchemaService extends IService<ApiSchema> {
    
    ApiSchema findByProjectIdAndKey(Long projectId, String definitionKey);
}
