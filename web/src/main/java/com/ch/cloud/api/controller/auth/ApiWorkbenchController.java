package com.ch.cloud.api.controller.auth;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.domain.ApiOperationLog;
import com.ch.cloud.api.domain.ApiPath;
import com.ch.cloud.api.domain.ApiPermission;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.domain.ApiSchema;
import com.ch.cloud.api.dto.ApiProjectInfoDTO;
import com.ch.cloud.api.service.IApiOperationLogService;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.cloud.api.service.IApiPermissionService;
import com.ch.cloud.api.service.IApiProjectService;
import com.ch.cloud.api.service.IApiProjectUserService;
import com.ch.cloud.api.service.IApiSchemaService;
import com.ch.cloud.upms.client.UpmsProjectClient;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.cloud.upms.dto.ProjectUserRoleDTO;
import com.ch.cloud.upms.dto.UserDto;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目管理
 *
 * @author Zhimin.Ma
 */
@RestController
@RequestMapping("/api/workbench")
@Tag(name = "api-workbench-controller", description = "工作台")
public class ApiWorkbenchController {
    
    @Autowired
    private IApiProjectService apiProjectService;
    
    @Autowired
    private IApiSchemaService apiSchemaService;
    
    @Autowired
    private IApiPathService apiPathService;
    
    @Autowired
    private UpmsProjectClient upmsProjectClient;
    
    @Autowired
    private UpmsUserClient upmsUserClient;
    
    @Autowired
    private IApiProjectUserService apiProjectUserService;
    
    @Autowired
    private IApiOperationLogService apiOperationLogService;
    
    @Autowired
    private IApiPermissionService apiPermissionService;
    
    /**
     * 工作台统计数据DTO
     */
    @Data
    public static class WorkbenchStatisticsDTO {
        
        private long apiCount;      // API数量
        
        private long modelCount;    // 模型数量
        
        private Integer memberCount;   // 成员数量
        
        private List<Integer> radarData; // 雷达图数据
    }
    
    /**
     * 最近接口更新DTO
     */
    @Data
    public static class RecentApiDTO {
        
        private Long id;
        
        private String method;
        
        private String path;
        
        private String title;
        
        private Date updateTime;
    }
    
    /**
     * 项目动态DTO
     */
    @Data
    public static class ActivityDTO {
        
        private UserDto user;
        
        private String content;
        
        private Date time;
    }
    
    @Operation(summary = "获取工作台统计数据", description = "获取工作台统计数据")
    @GetMapping("statistics")
    @HasPermission("workbench:statistics")
    public Result<WorkbenchStatisticsDTO> statistics(@CookieValue Long projectId) {
        return ResultUtils.wrapFail(() -> {
            WorkbenchStatisticsDTO statistics = new WorkbenchStatisticsDTO();
            
            // 统计API数量
            long apiCount = apiPathService.lambdaQuery().eq(ApiPath::getProjectId, projectId).count();
            statistics.setApiCount(apiCount);
            
            // 统计模型数量（这里用schema数量代替）
            long schemaCount = apiSchemaService.lambdaQuery().eq(ApiSchema::getProjectId, projectId).count();
            statistics.setModelCount(schemaCount);
            
            // 统计成员数量
            Result<ProjectUserRoleDTO> usersResult = upmsProjectClient.findUsers(projectId);
            int memberCount = 0;
            if (!usersResult.isEmpty()) {
                memberCount = usersResult.getRows().size();
            }
            statistics.setMemberCount(memberCount);
            
            // 雷达图数据（这里用模拟数据，实际可根据业务需求计算）
            statistics.setRadarData(Arrays.asList(85, 92, 78, 95, 88));
            
            return statistics;
        });
    }
    
    @Operation(summary = "获取最近接口更新", description = "获取最近接口更新列表")
    @GetMapping("recent-apis")
    @HasPermission("workbench:recent-apis")
    public Result<List<RecentApiDTO>> recentApis(@CookieValue Long projectId) {
        return ResultUtils.wrapFail(() -> {
            // 获取最近更新的API列表
            List<ApiPath> recentApis = apiPathService.lambdaQuery().eq(ApiPath::getProjectId, projectId)
                    .orderByDesc(ApiPath::getUpdateAt).last("LIMIT 10").list();
            
            return recentApis.stream().map(api -> {
                RecentApiDTO dto = new RecentApiDTO();
                dto.setId(api.getId());
                dto.setMethod(api.getMethod());
                dto.setPath(api.getPath());
                dto.setTitle(api.getName());
                dto.setUpdateTime(api.getUpdateAt());
                return dto;
            }).collect(Collectors.toList());
        });
    }
    
    @Operation(summary = "获取项目动态", description = "获取项目动态列表")
    @GetMapping("activities")
    @HasPermission("workbench:activities")
    public Result<List<ActivityDTO>> activities(@CookieValue Long projectId) {
        return ResultUtils.wrapFail(() -> {
            // 这里可以根据实际业务需求获取项目动态
            // 比如：API创建、更新、成员加入等操作记录
            // 暂时返回模拟数据
            List<ApiOperationLog> list = apiOperationLogService.lambdaQuery()
                    .eq(ApiOperationLog::getModule, projectId.toString()).orderByDesc(ApiOperationLog::getCreatedTime)
                    .list(Page.of(1, 10));
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> authCodes = list.stream().map(ApiOperationLog::getOperationType).collect(Collectors.toList());
            List<ApiPermission> permissions = apiPermissionService.lambdaQuery()
                    .in(ApiPermission::getCode, authCodes).list();
            Map<String, ApiPermission> permissionMap = permissions.stream()
                    .collect(Collectors.toMap(ApiPermission::getCode, Function.identity()));
            return list.stream().map(log -> {
                ActivityDTO activity = new ActivityDTO();
                Result<UserDto> userResult = upmsUserClient.findInfoByUsername(log.getUserId());
                if (!userResult.isEmpty()) {
                    UserDto userDto = userResult.get();
                    if (CommonUtils.isEmpty(userDto.getAvatar())) {
                        userDto.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + userDto.getUserId());
                    }
                    activity.setUser(userDto);
                    if (permissionMap.containsKey(log.getOperationType())) {
                        activity.setContent(permissionMap.get(log.getOperationType()).getName());
                    } else {
                        activity.setContent(log.getOperationType());
                    }
                    activity.setTime(log.getCreatedTime());
                }
                return activity;
            }).collect(Collectors.toList());
        });
    }
    
    
    @Operation(summary = "获取项目成员", description = "获取项目成员")
    @GetMapping(value = "members")
    @HasPermission("workbench:members")
    public Result<ProjectUserRoleDTO> members(@CookieValue Long projectId) {
        
        return ResultUtils.wrap(() -> {
            Result<ProjectDto> projectResult = upmsProjectClient.infoByIdOrCode(projectId, null);
            Assert.notEmpty(projectResult.getRows(), PubError.NOT_EXISTS, "projectId:" + projectId);
            Result<ProjectUserRoleDTO> usersResult = upmsProjectClient.findUsers(projectId);
            List<ProjectUserRoleDTO> users = Lists.newArrayList();
            if (!usersResult.isEmpty()) {
                users.addAll(usersResult.getRows());
            }
            List<String> visitorUserIds = apiProjectUserService.findVisitorUserIds(projectId);
            if (CommonUtils.isNotEmpty(visitorUserIds)) {
                users.addAll(visitorUserIds.stream().map(userId -> {
                    ProjectUserRoleDTO dto = new ProjectUserRoleDTO();
                    dto.setUserId(userId);
                    Result<UserDto> userResult = upmsUserClient.findInfoByUsername(userId);
                    if (!userResult.isEmpty()) {
                        dto.setRealName(userResult.get().getRealName());
                    }
                    dto.setRole("VISITOR");
                    return dto;
                }).collect(Collectors.toList()));
            }
            return users;
        });
    }
    
    
    @Operation(summary = "获取项目信息", description = "获取项目信息")
    @GetMapping(value = {"info"})
    @HasPermission("project:info")
    public Result<ApiProjectInfoDTO> info(@CookieValue Long projectId) {
        return ResultUtils.wrapFail(() -> {
            Result<ProjectDto> projectResult = upmsProjectClient.infoByIdOrCode(projectId, null);
            Assert.notEmpty(projectResult.getRows(), PubError.NOT_EXISTS, "项目:" + projectId);
            ApiProject record = apiProjectService.getByProjectId(projectId);
            ApiProjectInfoDTO dto = BeanUtil.copyProperties(record, ApiProjectInfoDTO.class);
            dto.setName(projectResult.get().getName());
            Result<UserDto> userResult = upmsUserClient.findInfoByUsername(projectResult.get().getManager());
            if (!userResult.isEmpty()) {
                dto.setOwner(userResult.get().getRealName());
            }
            return dto;
        });
    }
}
