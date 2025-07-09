package com.ch.cloud.api.controller.auth;

import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.dto.ApiResourceDTO;
import com.ch.cloud.api.manager.ApiShareManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "api-share-code-controller", description = "接口分享码服务")
@RestController
@RequestMapping("/api/share")
public class ApiShareCodeController {
    @Autowired
    private ApiShareManager apiShareManager;

    @Operation(summary = "生成接口分享码", description = "生成接口分享码")
    @PostMapping("/generate")
    @HasPermission(value = "share:generate")
    public String generate(@RequestBody List<ApiResourceDTO> resources) {
        return apiShareManager.createShareCode(resources);
    }

}
