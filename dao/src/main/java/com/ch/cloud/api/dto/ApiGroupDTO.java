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
public class ApiGroupDTO {
    
    private Long id;
    
    private Long parentId;
    
    private Long projectId;
    
    private String name;

    private String description;

    private String type;

    private String createdBy;

    private List<ApiGroupDTO> children;
}
