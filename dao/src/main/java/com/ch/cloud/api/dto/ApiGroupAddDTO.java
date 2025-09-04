package com.ch.cloud.api.dto;

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
public class ApiGroupAddDTO {

    private String type;

    private Long parentId;

    private Long projectId;

    private String name;

    private String description;
}
