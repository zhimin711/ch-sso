package com.ch.cloud.api.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiPermissionDTO {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "上级ID")
    private Long parentId;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "子节点")
    private List<ApiPermissionDTO> children;
}
