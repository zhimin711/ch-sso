package com.ch.cloud.api.dto;

import lombok.Data;

/**
 * <p>
 * desc: ApiProjectDTO
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/13
 */
@Data
public class ApiProjectRoleDTO {
    
    private String userId;
    
    private Long projectId;
    
    private String role;
}
