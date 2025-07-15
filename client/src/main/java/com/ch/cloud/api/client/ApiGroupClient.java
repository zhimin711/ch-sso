package com.ch.cloud.api.client;

import com.ch.cloud.api.pojo.GroupPath;
import com.ch.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhimin
 * @date 2025/1/10 17:07
 */
@FeignClient(name = "${feign.client.api:ch-sso}",url = "${feign.client.api.url:}", contextId = "apiModuleClient", path = "/c/api/group")
public interface ApiGroupClient {

    @GetMapping("modules")
    Result<GroupPath> modules(@RequestParam Long projectId);

    @GetMapping("paths")
    Result<GroupPath> paths(@RequestParam Long moduleId);
}
