package com.ch.cloud.api.controller.auth;

import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ch.cloud.api.domain.ApiSchema;
import com.ch.cloud.api.service.IApiSchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ch.cloud.api.annotation.HasPermission;

/**
 * 接口对象定义信息Controller
 * 
 * @author zhimin.ma
 * @since 2022-10-16 18:03:21
 */
@RestController
@RequestMapping("/api/schema")
@Tag(name = "api-schema-controller", description = "接口对象定义信息")
public class ApiSchemaController {

    @Autowired
    private IApiSchemaService apiSchemaService;

    @Operation(summary = "分页查询", description = "分页查询接口对象定义信息")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<ApiSchema> page(ApiSchema record,
                                      @PathVariable(value = "num") int pageNum,
                                      @PathVariable(value = "size") int pageSize) {
        return ResultUtils.wrapPage(() -> {
            return null;
        });
    }

    @Operation(summary = "添加", description = "添加接口对象定义信息")
    @PostMapping
    @HasPermission("api_schema_add")
    public Result<Boolean> add(@RequestBody ApiSchema record) {
        return ResultUtils.wrapFail(() -> apiSchemaService.save(record));
    }

    @Operation(summary = "修改", description = "修改接口对象定义信息")
    @PutMapping({"{id:[0-9]+}"})
    @HasPermission("api_schema_edit")
    public Result<Boolean> edit(@RequestBody ApiSchema record) {
        return ResultUtils.wrapFail(() -> apiSchemaService.updateById(record));
    }

    //@GetMapping({"{id:[0-9]+}"})
    public Result<ApiSchema> find(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> apiSchemaService.getById(id));
    }

    @Operation(summary = "删除", description = "删除接口对象定义信息")
    //@DeleteMapping({"{id:[0-9]+}"})
    @HasPermission("api_schema_delete")
    public Result<Boolean> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> apiSchemaService.removeById(id));
    }
}
