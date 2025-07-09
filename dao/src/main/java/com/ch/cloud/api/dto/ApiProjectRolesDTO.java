package com.ch.cloud.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * desc: ApiProjectDTO
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/14
 */
@Data
public class ApiProjectRolesDTO {
    
    private Long id;
    private String code;
    private String name;

    @Schema(description = "项目角色，多个为,拼接")
    private String role;
}
