package com.ch.cloud.api.controller.auth;

import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.service.IApiProjectUserService;
import com.ch.cloud.upms.client.UpmsProjectClient;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.cloud.upms.dto.ProjectUserRoleDTO;
import com.ch.cloud.upms.dto.UserDto;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 访客管理
 */
@RestController
@RequestMapping("/api/visitor")
@Tag(name = "api-visitor-controller", description = "访客管理")
public class ApiVisitorController {
    
    @Autowired
    private IApiProjectUserService apiProjectUserService;
    
    @Autowired
    private UpmsUserClient upmsUserClient;
    
    @Autowired
    private UpmsProjectClient projectClient;
    
    /**
     * 获取所有可添加为访客的用户（未加入当前项目的用户）
     */
    @Operation(summary = "获取可添加为访客的用户", description = "获取未加入当前项目的用户列表")
    @GetMapping("/members")
    @HasPermission("visitor:members")
    public Result<UserDto> candidates(@CookieValue Long projectId, @RequestParam(required = false) String username) {
        // 2. 获取所有用户（此处需你补充真实接口，伪代码如下）
        Collection<UserDto> allUsers = new ArrayList<>();
        Result<UserDto> allUserResult = upmsUserClient.listAvailable(username); // 你需补充真实接口
        if (!allUserResult.isEmpty()) {
            allUsers = allUserResult.getRows();
        }
        // 1. 查询当前项目已加入的所有用户ID
        List<String> joinedUserIds = apiProjectUserService.findVisitorUserIds(projectId);
        Result<ProjectUserRoleDTO> users = projectClient.findUsers(projectId);
        if (!users.isEmpty()) {
            users.getRows().forEach(u -> {
                joinedUserIds.add(u.getUserId());
            });
        }
        // 3. 过滤掉已加入项目的用户
        List<UserDto> candidates = allUsers.stream()
                .filter(u -> u.getUserId() != null && !joinedUserIds.contains(u.getUsername()))
                .collect(Collectors.toList());
        return Result.success(candidates);
    }
    
    /**
     * 获取当前项目访客列表
     */
    @Operation(summary = "获取访客列表", description = "获取当前项目访客列表")
    @GetMapping("/list")
    @HasPermission("visitor:list")
    public Result<UserDto> list(@CookieValue Long projectId) {
        // 1. 查询当前项目所有访客userId
        List<String> allUserIds = apiProjectUserService.findVisitorUserIds(projectId);
        // 2. 查询这些userId中，role=VISITOR 的用户
        // 3. 获取用户详情
        List<UserDto> visitors = new ArrayList<>();
        
        for (String userId : allUserIds) {
            Result<UserDto> result = upmsUserClient.findInfoByUsername(userId);
            if (!result.isEmpty()) {
                visitors.add(result.get());
            }
        }
        return Result.success(visitors);
    }
    
    /**
     * 添加访客
     */
    @Operation(summary = "添加访客", description = "添加访客到当前项目")
    @PostMapping("/add")
    @HasPermission("visitor:add")
    public Result<Boolean> add(@CookieValue Long projectId, @RequestBody List<String> userIds) {
        Assert.notEmpty(userIds, PubError.ARGS, "请选择访客");
        return ResultUtils.wrap(() -> userIds.forEach(userId -> {
            // 1. 检查是否已存在
            int exists = apiProjectUserService.exists(projectId, userId);
            if (exists > 0) {
                return;
            }
            // 2. 插入访客记录（你需扩展mapper方法实现插入）
            boolean ok = apiProjectUserService.addVisitor(projectId, userId);
        }));
    }
    
    /**
     * 移除访客
     */
    @Operation(summary = "移除访客", description = "从当前项目移除访客")
    @DeleteMapping("/{userId}")
    @HasPermission("visitor:del")
    public Result<Boolean> remove(@CookieValue Long projectId, @PathVariable String userId) {
        // 1. 删除访客记录（你需扩展mapper方法实现删除指定userId和role=VISITOR）
        
        boolean ok = apiProjectUserService.removeVisitor(projectId, userId);
        return Result.success(ok);
    }
} 