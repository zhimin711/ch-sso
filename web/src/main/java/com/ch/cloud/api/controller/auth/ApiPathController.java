package com.ch.cloud.api.controller.auth;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.domain.ApiPath;
import com.ch.cloud.api.dto.ApiGroupPathDetailDTO;
import com.ch.cloud.api.dto.ApiPathDTO;
import com.ch.cloud.api.enums.GroupType;
import com.ch.cloud.api.manager.ApiPathManager;
import com.ch.cloud.api.service.IApiGroupService;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ch.cloud.api.annotation.HasPermission;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API接口配置服务
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:35:57
 */
@RestController
@RequestMapping("/api/path")
@Tag(name = "api-path-controller", description = "API接口配置服务")
public class ApiPathController {
    
    @Autowired
    private IApiPathService apiPathService;
    
    @Autowired
    private ApiPathManager apiPathManager;
    
    @Autowired
    private IApiGroupService apiGroupService;
    
    @Operation(summary = "添加", description = "添加接口")
    @PostMapping
    @HasPermission("path:add")
    public Result<Boolean> add(@Validated @RequestBody ApiGroupPathDetailDTO record, @CookieValue Long projectId) {
        record.setProjectId(projectId);
        return ResultUtils.wrapFail(() -> apiPathManager.save(record));
    }
    
    @Operation(summary = "修改", description = "修改接口")
    @PutMapping({"{id:[0-9]+}"})
    @HasPermission("path:edit")
    public Result<Boolean> edit(@PathVariable Long id, @RequestBody ApiGroupPathDetailDTO record) {
        record.setId(id);
        return ResultUtils.wrapFail(() -> apiPathManager.update(record));
    }
    
    @GetMapping({"{id:[0-9]+}"})
    @HasPermission("path:detail")
    public ApiGroupPathDetailDTO find(@PathVariable Long id) {
        ApiPath apiPath = apiPathService.getById(id);
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
        List<Long> groupIds = apiPathManager.listGroupIds(apiPath.getId());
        List<ApiGroup> apiGroups = apiGroupService.listByIds(groupIds);
        Map<String, List<ApiGroup>> groupMap = apiGroups.stream().collect(Collectors.groupingBy(ApiGroup::getType));
        groupMap.forEach((k, v) -> {
            GroupType groupType = GroupType.fromCode(k);
            if (groupType == GroupType.MODULE) {
                dto.setGroupId(v.get(0).getId());
            } else if (groupType == GroupType.TAG) {
                dto.setTagGroupIds(v.stream().map(ApiGroup::getId).collect(Collectors.toList()));
            } else {
                dto.setCustomGroupIds(v.stream().map(ApiGroup::getId).collect(Collectors.toList()));
            }
        });
        return dto;
    }
    
    @PostMapping({"batch-get"})
    @HasPermission("path:batch-get")
    public List<ApiPathDTO> batchGet(@RequestBody List<Long> ids) {
        List<ApiPath> config = apiPathService.listByIds(ids);
        return BeanUtil.copyToList(config, ApiPathDTO.class);
    }
    
    @Operation(summary = "删除", description = "删除接口")
    @DeleteMapping({"{id:[0-9]+}"})
    @HasPermission("path:delete")
    public Result<Boolean> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> apiPathManager.delete(id));
    }
    
}
