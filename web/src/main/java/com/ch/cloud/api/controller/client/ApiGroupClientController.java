package com.ch.cloud.api.controller.client;

import cn.hutool.core.bean.BeanUtil;
import com.ch.cloud.api.client.ApiGroupClient;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.domain.ApiPath;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.pojo.GroupPath;
import com.ch.cloud.api.service.IApiGroupService;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.cloud.api.service.IApiProjectService;
import com.ch.core.utils.StrUtil;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CharUtils;
import com.ch.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhimin
 * @date 2025/3/23 14:05
 */
@Tag(name = "api-group-client-controller", description = "接口分组(内部调用)")
@RestController
@RequestMapping("/c/api/group")
public class ApiGroupClientController implements ApiGroupClient {
    
    @Autowired
    private IApiGroupService apiGroupService;
    
    @Autowired
    private IApiPathService apiPathService;
    
    @Autowired
    private IApiProjectService apiProjectService;
    
    @Operation(summary = "查询模块列表", description = "查询模块列表")
    @GetMapping("/modules")
    @Override
    public Result<GroupPath> modules(@RequestParam Long projectId) {
        return ResultUtils.wrap(() -> {
            List<ApiGroup> apiGroups = apiGroupService.listApiGroupByProjectId(projectId);
            apiGroups.forEach(apiGroup -> {
                apiGroup.setName(CommonUtils.or(apiGroup.getAlias(), apiGroup.getName()));
            });
            return BeanUtil.copyToList(apiGroups, GroupPath.class);
        });
    }
    
    @Operation(summary = "查询路径列表", description = "查询路径列表")
    @GetMapping("/paths")
    @Override
    public Result<GroupPath> paths(@RequestParam Long moduleId) {
        return ResultUtils.wrap(() -> {
            List<Long> pathIds = apiGroupService.listPathIdsByGroupId(moduleId);
            List<ApiPath> apiPaths = apiPathService.listByIds(pathIds);
            if (apiPaths.isEmpty()) {
                return null;
            }
            List<GroupPath> groupPaths = apiPaths.stream().map(apiPath -> {
                GroupPath dto = BeanUtil.copyProperties(apiPath, GroupPath.class);
                
//                boolean chinese = CharUtils.containsChinese(apiPath.getName());
//                if (chinese) {
                    // 将路径中的特殊字符替换为下划线并设置到 Key 中
                    String key = apiPath.getPath().replaceAll("[^a-zA-Z0-9]", "_");
                    dto.setKey(key);
//                }else {
//                    dto.setKey(apiPath.getName());
//                }
                return dto;
            }).collect(Collectors.toList());
            ApiProject project = apiProjectService.getByProjectId(apiPaths.get(0).getProjectId());
            if (CommonUtils.isNotEmpty(project.getBasePath())) {
                groupPaths.forEach(apiPath -> {
                    //                    apiPath.setKey(project.get);
                    String path = StrUtil.linkStr("/", project.getBasePath(), apiPath.getPath());
                    apiPath.setPath(path);
                });
            }
            return groupPaths;
        });
    }
}
