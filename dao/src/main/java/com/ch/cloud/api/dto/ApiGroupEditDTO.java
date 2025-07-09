package com.ch.cloud.api.dto;

import lombok.Data;

/**
 * <p>
 * desc: ApiGroupDTO
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/14
 */
@Data
public class ApiGroupEditDTO {

    private String type;

    private Long parentId;

    private String name;

    private String description;
}
