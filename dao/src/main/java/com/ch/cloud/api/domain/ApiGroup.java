package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.OrderBy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;


/**
 * 接口分组对象 bt_api_group
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:31:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "接口分组")
@TableName("bt_api_group")
public class ApiGroup extends BaseEntity {

    /**
     * parentId: 0为1级
     */
    @Schema(description = "parentId")
    @TableField("parent_id")
    private Long parentId;

    /**
     * projectId
     */
    @Schema(description = "projectId")
    @TableField("project_id")
    private Long projectId;

    /**
     * 名称
     */
    @Schema(description = "代码")
    @TableField("code")
    @NotBlank
    private String code;
    
    @Schema(description = "名称")
    @TableField("name")
    @NotBlank
    private String name;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @TableField("description")
    private String description;
    
    @Schema(description = "排序")
    @TableField("sort")
    @OrderBy
    private Integer sort;
    
    @Schema(description = "类型：1.接口分组 2.标签分组 3.自定义分组")
    @TableField("type")
    private String type;

}
