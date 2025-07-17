package com.ch.cloud.api.manager.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.api.dto.ApiResourceDTO;
import com.ch.cloud.api.manager.ApiShareManager;
import com.ch.cloud.api.props.ApiAuthProperties;
import com.ch.cloud.upms.client.UpmsAuthCodeClient;
import com.ch.cloud.upms.dto.AuthCodeGenerateDTO;
import com.ch.cloud.upms.dto.AuthCodeResourceDTO;
import com.ch.cloud.upms.dto.AuthCodeVO;
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
    private ApiAuthProperties apiAuthProperties;

    @Override
    public String createShareCode(List<ApiResourceDTO> resources) {
        AuthCodeGenerateDTO auth = new AuthCodeGenerateDTO();
        auth.setAuthUser(ContextUtil.getUserId());
        auth.setExpireTime(DateUtils.addDays(DateUtils.current(), apiAuthProperties.getDays()));

        JSONObject json = new JSONObject();
        json.put("permissions", apiAuthProperties.getPermissions());
        json.put("resources", resources);
        auth.setContent(json.toJSONString());
        Result<AuthCodeVO> genResult = upmsAuthCodeClient.generate(auth);

        return genResult.get().getCode();
    }

    @Override
    public List<ApiResourceDTO> getResources(String shareCode) {
        Result<AuthCodeResourceDTO> result = upmsAuthCodeClient.getContent(shareCode);
        if (result.isSuccess()) {
            AuthCodeResourceDTO resourceDTO = result.get();

            JSONObject jsonObject = JSON.parseObject(resourceDTO.getContent());
            List<ApiResourceDTO> resources = jsonObject.getList("resources", ApiResourceDTO.class);
            return resources;
        }
        return Lists.newArrayList();
    }
}
