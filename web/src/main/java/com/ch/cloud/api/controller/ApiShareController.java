package com.ch.cloud.api.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.domain.ApiPath;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.dto.ApiGroupPathDetailDTO;
import com.ch.cloud.api.dto.ApiResourceDTO;
import com.ch.cloud.api.dto.EnvDTO;
import com.ch.cloud.api.manager.ApiGroupManager;
import com.ch.cloud.api.manager.ApiShareManager;
import com.ch.cloud.api.pojo.GroupPath;
import com.ch.cloud.api.service.IApiGroupService;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.cloud.api.service.IApiProjectService;
import com.ch.cloud.api.service.ApiTenantManager;
import com.ch.cloud.api.utils.ApiUtil;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 接口分组Controller
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:31:22
 */
@RestController
@RequestMapping("/api/share")
@Tag(name = "api-share-controller", description = "接口分享服务")
public class ApiShareController {

    @Autowired
    private IApiGroupService apiGroupService;

    @Autowired
    private IApiPathService apiPathService;

    @Autowired
    private ApiGroupManager apiGroupManage;

    @Autowired
    private ApiShareManager apiShareManager;

    @Autowired
    private IApiProjectService apiProjectService;

    @Autowired
    private ApiTenantManager apiTenantManager;

    @Operation(summary = "查询分享接口树", description = "查询分享接口树")
    @GetMapping(value = {"tree"})
    public Result<GroupPath> tree(@RequestParam String apiKey) {
        return ResultUtils.wrap(() -> {
            List<ApiResourceDTO> resources = apiShareManager.getResources(apiKey);
            Assert.notEmpty(resources, PubError.NOT_EXISTS, "分享资源");
            Map<Long, ApiResourceDTO> resourceMap = resources.stream()
                    .collect(Collectors.toMap(ApiResourceDTO::getGroupId, Function.identity()));
            List<ApiGroup> apiGroups = apiGroupService.listByIds(resourceMap.keySet());
            if (CommonUtils.isNotEmpty(apiGroups)) {
                List<GroupPath> tree = apiGroupManage.convertTree(apiGroups, resourceMap);
                return tree;
            }
            return Lists.newArrayList();
        });
    }

    @Operation(summary = "查询接口详情", description = "查询接口详情")
    @GetMapping(value = {"detail"})
    public Result<ApiGroupPathDetailDTO> detail(@RequestParam String apiKey, @RequestParam Long pathId) {
        return ResultUtils.wrap(() -> {
            List<ApiResourceDTO> resources = apiShareManager.getResources(apiKey);
            boolean has = resources.parallelStream().anyMatch(resource -> resource.getApiIds().contains(pathId));
            Assert.isTrue(has, PubError.NOT_EXISTS, "分享资源");
            ApiPath apiPath = apiPathService.getById(pathId);
            ApiGroupPathDetailDTO dto = new ApiGroupPathDetailDTO();

            BeanUtils.copyProperties(apiPath, dto);
            if (CommonUtils.isNotEmpty(apiPath.getMethod())) {
                dto.setMethod(apiPath.getMethod().toUpperCase());
            }
            if (CommonUtils.isNotEmpty(apiPath.getParameters())) {
                dto.setParameters(JSON.parseObject(apiPath.getParameters()));
            }
            if (CommonUtils.isNotEmpty(apiPath.getResponses())) {
                dto.setResponses(JSON.parseObject(apiPath.getResponses()));
            }
            if (CommonUtils.isNotEmpty(apiPath.getBody())) {
                dto.setRequestBody(JSON.parseObject(apiPath.getBody()));
            }
            ApiProject project = apiProjectService.getByProjectId(apiPath.getProjectId());
            List<EnvDTO> list = buildEnvList(project);
            dto.setEnvList(list);
            return dto;
        });
    }

    private List<EnvDTO> buildEnvList(ApiProject project) {
        // 优先使用项目的env字段配置
        if (CommonUtils.isNotEmpty(project.getEnv()) && JSON.isValidObject(project.getEnv())) {
            JSONObject envJson = JSON.parseObject(project.getEnv());
            if (envJson.containsKey("envList")) {
                List<EnvDTO> envList = envJson.getList("envList", EnvDTO.class);
                JSONObject envPrefix = envJson.containsKey("envPrefix") ? envJson.getJSONObject("envPrefix") : new JSONObject();
                envList.forEach(env -> {
                    env.setDomain(ApiUtil.handleDomain(env.getDomain()));
                    String basePath = ApiUtil.handlePrefix(envPrefix.getString("envPrefix_" + env.getDomain()));
                    if (CommonUtils.isEmpty(basePath)) {
                        basePath += ApiUtil.handlePrefix(project.getBasePath());
                    }
                    env.setPrefix(basePath);
                });
                return envList;
            }
        }
        
        // 如果项目没有配置，则使用租户默认配置
        if (project.getWorkspaceId() != null) {
            try {
                List<com.ch.cloud.api.dto.MergedEnvConfigDTO> mergedConfigs = apiTenantManager.getProjectEnvConfigs(project.getProjectId());
                if (mergedConfigs != null && !mergedConfigs.isEmpty()) {
                    List<EnvDTO> envList = Lists.newArrayList();
                    for (com.ch.cloud.api.dto.MergedEnvConfigDTO config : mergedConfigs) {
                        EnvDTO env = new EnvDTO();
                        env.setId(Long.valueOf(config.getEnvKey().hashCode()));
                        env.setName(config.getName());
                        env.setDomain(ApiUtil.handleDomain(config.getDomain()));
                        env.setPrefix(ApiUtil.handlePrefix(config.getPrefix()));
                        envList.add(env);
                    }
                    return envList;
                }
            } catch (Exception e) {
                // 如果获取租户配置失败，返回空列表
            }
        }
        
        return Lists.newArrayList();
    }
}
