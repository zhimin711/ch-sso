package com.ch.cloud.api.dto;

import lombok.Data;

/**
 * <p>
 * desc: ApiParamDTO
 * </p>
 *
 * @author zhimin
 * @since 2025/6/17 17:08
 */
@Data
public class ApiParamDTO {
    
    private String name;
    
    private String description;
    
    private boolean required;
    
    private String type;
    
    private String format;
    
    private String example;
}
