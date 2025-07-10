package com.ch.cloud.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ch.cloud.api.domain.ApiPermission;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.dto.ApiPermissionDTO;
import com.ch.cloud.api.dto.ApiProjectRoleDTO;
import com.ch.cloud.api.dto.ApiProjectRolesDTO;
import com.ch.cloud.api.enums.ProjectRoleType;
import com.ch.cloud.api.manager.ApiGroupManager;
import com.ch.cloud.api.service.IApiPermissionService;
import com.ch.cloud.api.service.IApiProjectService;
import com.ch.cloud.upms.client.UpmsProjectClient;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.cloud.upms.dto.ProjectRoleDto;
import com.ch.cloud.upms.enums.RoleType;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目管理
 *
 * @author Zhimin.Ma
 */
@RestController
@RequestMapping("/api/project")
@Tag(name = "api-project-controller", description = "接口项目配置")
public class ApiProjectController {
    
    @Autowired
    private UpmsUserClient upmsUserClient;
    
    @Autowired
    private UpmsProjectClient upmsProjectClient;
    
    @Autowired
    private IApiProjectService apiProjectService;
    
    @Autowired
    private ApiGroupManager apiGroupManager;
    
    @Autowired
    private IApiPermissionService apiPermissionService;
    
    
    @GetMapping(value = {"my"})
    public List<ApiProjectRolesDTO> my() {
        String user = ContextUtil.getUserId();
        Result<ProjectRoleDto> result = upmsUserClient.findProjectsByUserId(user);
        if (result.isEmpty()) {
            return Lists.newArrayList();
        }
        return result.getRows().stream().map(e -> BeanUtil.copyProperties(e, ApiProjectRolesDTO.class))
                .collect(Collectors.toList());
    }
    
    @GetMapping(value = {"other"})
    public Result<ProjectDto> other() {
        List<Long> ids = apiProjectService.findByUserId(ContextUtil.getUsername());
        if (ids.isEmpty()) {
            return Result.success();
        }
        return upmsProjectClient.infoByIds(ids);
    }
    
    
    @Operation(summary = "获取项目角色权限", description = "获取项目角色权限")
    @GetMapping(value = "permissions")
    public List<ApiPermissionDTO> getPermissions(@RequestParam Long projectId, @RequestParam String roleCode) {
        ProjectRoleType roleType = ProjectRoleType.fromName(roleCode);
        ApiProjectRoleDTO roleDTO = apiProjectService.findByUserIdAndProjectId(ContextUtil.getUsername(), projectId);
        if (roleDTO == null || !CommonUtils.isEquals(roleDTO.getRole(), roleType.name())) {
            List<RoleType> projectRoles = upmsUserClient.listProjectRoles(ContextUtil.getUsername(), projectId, roleCode);
            Assert.notEmpty(projectRoles, PubError.NOT_ALLOWED, roleCode);
        }
        Assert.isFalse(RoleType.isVisitor(roleCode) && roleDTO == null, PubError.NOT_ALLOWED, roleCode, "授权访问");
        
        List<ApiPermission> list = apiPermissionService.lambdaQuery().like(ApiPermission::getRoles, roleType.name())
                .list();
        Map<Long, List<ApiPermissionDTO>> permissionsMap = list.stream()
                .map(e -> BeanUtil.copyProperties(e, ApiPermissionDTO.class))
                .collect(Collectors.groupingBy(ApiPermissionDTO::getParentId));
        permissionsMap.forEach((k, v) -> {
            v.forEach(e -> {
                if (permissionsMap.containsKey(e.getId())) {
                    List<ApiPermissionDTO> children = permissionsMap.get(e.getId());
                    e.setChildren(children);
                }
            });
        });
        ApiProject project = apiProjectService.getByProjectId(projectId);
        // 如果项目不存在，则创建，并添加默认分组
        if (project == null) {
            project = new ApiProject();
            project.setProjectId(projectId);
            //            project.setName(projectId.toString());
            apiProjectService.save(project);
            apiGroupManager.createDefaultGroup(projectId);
        }
        return permissionsMap.get(0L);
    }
    
    
}
