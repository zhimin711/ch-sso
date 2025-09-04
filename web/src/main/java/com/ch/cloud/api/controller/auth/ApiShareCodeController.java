package com.ch.cloud.api.controller.auth;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.domain.ApiShareCode;
import com.ch.cloud.api.dto.ApiResourceDTO;
import com.ch.cloud.api.dto.ApiShareCodeQueryDTO;
import com.ch.cloud.api.dto.ApiShareCodeVO;
import com.ch.cloud.api.manager.ApiShareManager;
import com.ch.cloud.api.service.IApiShareCodeService;
import com.ch.core.result.Result;
import com.ch.toolkit.ContextUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "api-share-code-controller", description = "接口分享码服务")
@RestController
@RequestMapping("/api/share")
public class ApiShareCodeController {
    @Autowired
    private ApiShareManager apiShareManager;
    
    @Autowired
    private IApiShareCodeService apiShareCodeService;

    @Operation(summary = "生成接口分享码", description = "生成接口分享码")
    @PostMapping("/generate")
    @HasPermission(value = "share:generate")
    public Result<String> generate(@CookieValue Long projectId, @RequestBody List<ApiResourceDTO> resources) {
        return Result.from(() -> apiShareManager.createShareCode(projectId, resources));
    }
    
    @Operation(summary = "分页查询接口分享码", description = "分页查询接口分享码")
    @GetMapping("/page")
    @HasPermission(value = "share:page")
    public Result<IPage<ApiShareCodeVO>> page(
            @CookieValue Long projectId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) String userId,
            @Parameter(description = "分享码") @RequestParam(required = false) String shareCode) {
        return Result.from(() -> {
            ApiShareCodeQueryDTO queryDTO = new ApiShareCodeQueryDTO();
            queryDTO.setProjectId(projectId);
            queryDTO.setPageNum(pageNum);
            queryDTO.setPageSize(pageSize);
            queryDTO.setUserId(userId);
            queryDTO.setShareCode(shareCode);
            
            IPage<ApiShareCode> page = apiShareCodeService.pageByQuery(queryDTO);
            
            // 转换为VO
            IPage<ApiShareCodeVO> voPage = page.convert(ApiShareCodeVO::from);
            
            return voPage;
        });
    }
    
    @Operation(summary = "查询分享码详情", description = "根据ID查询分享码详情")
    @GetMapping("/{id}")
    @HasPermission(value = "share:detail")
    public Result<ApiShareCodeVO> detail(@Parameter(description = "分享码ID") @PathVariable Long id) {
        return Result.from(() -> {
            ApiShareCode shareCode = apiShareCodeService.getById(id);
            if (shareCode == null) {
                return null;
            }
            // 只能查看自己的分享码
            if (!shareCode.getUserId().equals(ContextUtil.getUserId())) {
                return null;
            }
            return ApiShareCodeVO.from(shareCode);
        });
    }
    
    @Operation(summary = "删除接口分享码", description = "删除接口分享码")
    @DeleteMapping("/{id}")
    @HasPermission(value = "share:delete")
    public Result<Boolean> delete(@Parameter(description = "分享码ID") @PathVariable Long id) {
        return Result.from(() -> {
            ApiShareCode shareCode = apiShareCodeService.getById(id);
            if (shareCode == null) {
                return false;
            }
            // 只能删除自己的分享码
            if (!shareCode.getUserId().equals(ContextUtil.getUserId())) {
                return false;
            }
            return apiShareCodeService.removeById(id);
        });
    }
}
