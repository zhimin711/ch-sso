package com.ch.cloud.api.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.ch.cloud.api.domain.ApiShareCode;
import com.ch.cloud.api.dto.ApiResourceDTO;
import com.ch.cloud.api.manager.ApiShareManager;
import com.ch.cloud.api.props.ApiAuthProperties;
import com.ch.cloud.api.service.IApiShareCodeService;
import com.ch.cloud.upms.client.UpmsAuthCodeClient;
import com.ch.cloud.upms.dto.AuthCodeGenerateDTO;
import com.ch.cloud.upms.dto.AuthCodeVO;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.DateUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiShareManagerImpl implements ApiShareManager {

    @Autowired
    private UpmsAuthCodeClient upmsAuthCodeClient;
    @Autowired
    private IApiShareCodeService apiShareCodeService;


    @Autowired
    private ApiAuthProperties apiAuthProperties;

    @Override
    public String createShareCode(Long projectId, List<ApiResourceDTO> resources) {
        AuthCodeGenerateDTO auth = new AuthCodeGenerateDTO();
        auth.setAuthUser(ContextUtil.getUserId());
        auth.setExpireTime(DateUtils.addDays(DateUtils.current(), apiAuthProperties.getDays()));

        Assert.notEmpty(apiAuthProperties.getPermissions(), PubError.INVALID, "未配置分享权限");
        JSONObject json = new JSONObject();
        json.put("permissions", apiAuthProperties.getPermissions());
        // json.put("resources", resources);
        auth.setContent(json.toJSONString());
        Result<AuthCodeVO> genResult = upmsAuthCodeClient.generate(auth);
        Assert.isTrue(genResult.isSuccess(), PubError.CREATE, genResult.getMessage());
        String code = genResult.get().getCode();

        ApiShareCode entity = BeanUtil.copyProperties(auth, ApiShareCode.class);
        entity.setShareCode(code);
        entity.setProjectId(projectId);
        entity.setResources(JSON.toJSONString(resources));
        entity.setExpireTime(auth.getExpireTime());
        entity.setUserId(auth.getAuthUser());
        apiShareCodeService.save(entity);
        return code;
    }

    @Override
    public List<ApiResourceDTO> getResources(String shareCode) {
        ApiShareCode apiShareCode = apiShareCodeService.lambdaQuery().eq(ApiShareCode::getShareCode, shareCode).one();
        if (apiShareCode == null) {
            return Lists.newArrayList();
        }
        
        // 手动处理 JSON 反序列化
        try {
            String resourcesJson = apiShareCode.getResources();
            if (resourcesJson == null || resourcesJson.trim().isEmpty()) {
                return Lists.newArrayList();
            }
            
            List<ApiResourceDTO> resources = JSON.parseArray(resourcesJson, ApiResourceDTO.class);
            return resources != null ? resources : Lists.newArrayList();
        } catch (Exception e) {
            // 如果反序列化出错，返回空列表
            return Lists.newArrayList();
        }
    }
}
