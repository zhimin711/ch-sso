package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.service.IApiSchemaService;
import com.ch.utils.CommonUtils;
import org.springframework.stereotype.Service;
import com.ch.cloud.api.mapper.ApiSchemaMapper;
import com.ch.cloud.api.domain.ApiSchema;

/**
 * 接口对象定义信息Service业务层
 *
 * @author zhimin.ma
 * @since 2022-10-16 18:03:21
 */
@Service
public class ApiSchemaServiceImpl extends ServiceImpl<ApiSchemaMapper, ApiSchema>
        implements IApiSchemaService {
    
    @Override
    public ApiSchema findByProjectIdAndKey(Long projectId, String definitionKey) {
        if (CommonUtils.isEmptyOr(projectId, definitionKey)) {
            return null;
        }
        ApiSchema param3 = new ApiSchema();
        param3.setProjectId(projectId);
        param3.setDefKey(definitionKey);
        return getOne(Wrappers.query(param3));
    }
}
