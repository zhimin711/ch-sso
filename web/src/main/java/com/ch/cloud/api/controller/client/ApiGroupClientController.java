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
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
            ApiProject project = apiProjectService.getByProjectId(apiPaths.get(0).getProjectId());
            if (CommonUtils.isNotEmpty(project.getBasePath())) {
                apiPaths.forEach(apiPath -> {

                    apiPath.setPath(project.getBasePath() + apiPath.getPath());
                });
            }
            return BeanUtil.copyToList(apiPaths, GroupPath.class);
        });
    }
}
