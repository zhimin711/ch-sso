package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/11/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "分组接口配置信息")
public class ApiGroupPathDetailDTO extends ApiPathDTO {

    @Schema(description = "分组ID")
    @NotNull
    private Long groupId;

    @Schema(description = "标签分组ID")
    private List<Long> tagGroupIds;

    @Schema(description = "自定义分组ID")
    private List<Long> customGroupIds;
}
