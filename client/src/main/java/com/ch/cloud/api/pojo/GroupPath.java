package com.ch.cloud.api.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * desc: ApiGroupDTO
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/14
 */
@Data
public class GroupPath {

    private Long id;

    @Schema(description = "唯一键")
    private String key;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "接口路径")
    private String path;

    @Schema(description = "接口方法")
    private String method;

    @Schema(description = "类型：1.接口分组 2.标签分组 3.自定义分组 -1.接口")
    private Integer type;

    private List<GroupPath> children;
}
