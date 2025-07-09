package com.ch.cloud.api.controller.auth;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.domain.ApiProject;
import com.ch.cloud.api.dto.ApiProjectDTO;
import com.ch.cloud.api.dto.ApiProjectInfoDTO;
import com.ch.cloud.api.enums.APIDocType;
import com.ch.cloud.api.manager.ApiGroupManager;
import com.ch.cloud.api.service.IApiProjectService;
import com.ch.cloud.upms.client.UpmsProjectClient;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.cloud.upms.dto.UserDto;
import com.ch.core.result.Result;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.utils.CommonUtils;
import com.ch.utils.JSONUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 项目管理
 *
 * @author Zhimin.Ma
 */
@RestController
@RequestMapping("/api/project")
@Tag(name = "api-project-auth-controller", description = "接口项目配置")
public class ApiProjectAuthController {

    @Autowired
    private IApiProjectService apiProjectService;

    @Autowired
    private ApiGroupManager apiGroupManager;

    @Autowired
    private UpmsProjectClient upmsProjectClient;

    @Autowired
    private UpmsUserClient upmsUserClient;

    @Operation(summary = "获取配置项目", description = "获取配置项目", tags = {"OPEN API 2.0"})
    @GetMapping(value = "settings")
    public Result<ApiProjectDTO> settings(@CookieValue Long projectId) {

        return Result.from(() -> {
            com.ch.result.Result<ProjectDto> projectResult = upmsProjectClient.infoByIdOrCode(projectId, null);
            Assert.notEmpty(projectResult.getRows(), PubError.NOT_EXISTS, "projectId:" + projectId);
            ProjectDto projectDto = projectResult.get();
            ApiProjectDTO dto = new ApiProjectDTO();
            ApiProject projectConfig = apiProjectService.getByProjectId(projectId);
            if (projectConfig != null) {
                BeanUtils.copyProperties(projectConfig, dto);
                if (CommonUtils.isNotEmpty(projectConfig.getEnv())) {
                    dto.setEnv(JSON.parseObject(projectConfig.getEnv()));
                }
            }
            dto.setProjectId(projectId);
            dto.setName(projectDto.getName());
            dto.setOwner(projectDto.getManager());
            com.ch.result.Result<UserDto> userResult = upmsUserClient.findInfoByUsername(projectDto.getManager());
            if (!userResult.isEmpty()) {
                dto.setOwner(userResult.get().getRealName());
            }
            return dto;
        });
    }

    @Operation(summary = "保存配置项目", description = "保存配置项目")
    @PostMapping("settings")
    @HasPermission("project:settings:save")
    public Result<Boolean> settings(@CookieValue Long projectId, @RequestBody ApiProjectDTO dto) {

        return Result.from(() -> {
            ApiProject projectConfig = apiProjectService.getByProjectId(projectId);
            Assert.notNull(projectConfig, PubError.NOT_EXISTS, "projectId:" + projectId);
            ApiProject record = BeanUtil.copyProperties(dto, ApiProject.class);
            record.setId(projectConfig.getId());
            //        record.setProjectId(projectId);
            if (record.getEnv() != null) {
                record.setEnv(JSON.toJSONString(dto.getEnv()));
            }

            return apiProjectService.updateById(record);
        });
    }

    @Operation(summary = "获取项目信息", description = "获取项目信息")
    @GetMapping(value = {"info"})
    @HasPermission("project:info")
    public Result<ApiProjectInfoDTO> info(@CookieValue Long projectId) {
        return Result.from(() -> {
            com.ch.result.Result<ProjectDto> projectResult = upmsProjectClient.infoByIdOrCode(projectId, null);
            Assert.notEmpty(projectResult.getRows(), PubError.NOT_EXISTS, "项目:" + projectId);
            ApiProject record = apiProjectService.getByProjectId(projectId);
            ApiProjectInfoDTO dto = BeanUtil.copyProperties(record, ApiProjectInfoDTO.class);
            dto.setName(projectResult.get().getName());
            com.ch.result.Result<UserDto> userResult = upmsUserClient.findInfoByUsername(projectResult.get().getManager());
            if (!userResult.isEmpty()) {
                dto.setOwner(userResult.get().getRealName());
            }
            return dto;
        });
    }

    /**
     * 导入API文档（支持上传文件或url，二选一，优先文件）
     *
     * @param projectId 项目ID
     * @param type      文档类型(openapi/swagger2)
     * @param url       文档URL（可选）
     * @param file      上传的swagger/openapi json文件（可选）
     * @return Boolean
     */
    @PostMapping("import")
    @HasPermission("project:import")
    public Boolean importData(@CookieValue Long projectId, @RequestParam String type,
                              @RequestParam(required = false) String url,
                              @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        Assert.notNull(projectId, PubError.NOT_EXISTS, "projectId不能为空");
        Assert.notEmpty(type, PubError.CONFIG, "type不能为空");

        APIDocType docType = APIDocType.fromValue(type.toLowerCase());
        Assert.notNull(docType, PubError.ARGS, "不支持的API文档类型:" + type);

        String docJson = null;
        // 优先处理上传文件
        if (file != null && !file.isEmpty()) {
            docJson = new String(file.getBytes(), StandardCharsets.UTF_8);
        } else if (CommonUtils.isNotEmpty(url)) {
            docJson = HttpUtil.get(url);
        } else {
            throw new IllegalArgumentException("请上传API文档文件或填写url");
        }
        Assert.isTrue(JSONUtils.isObject(docJson), PubError.INVALID, "API文档内容不是合法JSON");
        JSONObject obj = JSON.parseObject(docJson);

        if (obj.containsKey("swagger")) {
            docType = APIDocType.SWAGGER;
        } else if (obj.containsKey("openapi")) {
            docType = APIDocType.OPENAPI;
        }

        if (docType == APIDocType.OPENAPI || docType == APIDocType.SWAGGER) {
            apiGroupManager.parseDocTags(projectId, obj);
            // swagger 结构需要先解析，再解析地址
            if (docType == APIDocType.SWAGGER) {
                apiGroupManager.parseDocDefinitions(projectId, obj);
            } else {
                apiGroupManager.parseDocSchemas(projectId, obj);
            }
            apiGroupManager.parsePathAndTag(projectId, obj);
        }
        return true;
    }


}
