package com.ch.cloud.api.controller.auth;

import cn.hutool.core.bean.BeanUtil;
import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.dto.ApiGroupDTO;
import com.ch.cloud.api.enums.GroupType;
import com.ch.cloud.api.manager.ApiGroupManager;
import com.ch.cloud.api.pojo.GroupPath;
import com.ch.cloud.api.service.IApiGroupService;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 接口分组Controller
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:31:22
 */
@RestController
@RequestMapping("/api/group")
@Tag(name = "api-group-controller", description = "接口分组")
public class ApiGroupController {
    
    @Autowired
    private IApiGroupService apiGroupService;
    
    @Autowired
    private IApiPathService apiPathService;
    
    @Autowired
    private ApiGroupManager apiGroupManage;
    
    @Operation(summary = "分页查询", description = "分页查询接口分组")
    @GetMapping(value = {"tree"})
    @HasPermission(value = "group:tree")
    public Result<GroupPath> tree(@CookieValue Long projectId, @RequestParam GroupType type,
            @RequestParam(required = false, defaultValue = "false") Boolean withApi) {
        return ResultUtils.wrap(() -> {
            List<ApiGroup> apiGroups = Lists.newArrayList();
            switch (type) {
                case MODULE:
                    apiGroups = apiGroupService.listApiGroupByProjectId(projectId);
                    break;
                case TAG:
                    apiGroups = apiGroupService.listTagGroupByProjectId(projectId);
                    break;
                case CUSTOM:
                    apiGroups = apiGroupService.listMyGroupByProjectId(projectId, ContextUtil.getUserId());
                    break;
            }
            if (CommonUtils.isNotEmpty(apiGroups)) {
                return apiGroupManage.convertTree(apiGroups, withApi);
            }
            return Lists.newArrayList();
        });
    }
    
    @Operation(summary = "添加", description = "添加接口分组")
    @PostMapping
    @HasPermission("group:add")
    public Result<Boolean> add(@Validated @RequestBody ApiGroupDTO record, @CookieValue Long projectId) {
        record.setProjectId(projectId);
        if (CommonUtils.isEmpty(record.getParentId())) {
            record.setParentId(0L);
        }
        return ResultUtils.wrapFail(() -> {
            ApiGroup apiGroup = BeanUtil.copyProperties(record, ApiGroup.class);
            apiGroup.setCode(apiGroup.getName());
            return apiGroupService.save(apiGroup);
        });
    }
    
    @Operation(summary = "修改", description = "修改接口分组")
    @PutMapping({"{id:[0-9]+}"})
    @HasPermission("group:edit")
    public Result<Boolean> edit(@PathVariable Long id, @RequestBody ApiGroupDTO record) {
        return ResultUtils.wrapFail(
                () -> apiGroupService.lambdaUpdate().eq(ApiGroup::getId, id)
                        .set(ApiGroup::getCode, record.getName())
                        .set(ApiGroup::getName, record.getName())
                        .set(ApiGroup::getDescription, record.getDescription())
                        .set(CommonUtils.isNotEmpty(record.getParentId()), ApiGroup::getParentId, record.getParentId())
                        .update());
    }
    
    @Operation(summary = "删除", description = "删除接口分组")
    @DeleteMapping({"{id:[0-9]+}"})
    @HasPermission("group:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> apiGroupManage.delete(id));
    }
}
