package com.ch.cloud.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/11/15
 */
@Data
public class ApiPropertyDTO {
    
    private String name;
    
    private String type;
    
    private String description;
    
    private String format;
    
    private Boolean required;
    
    private String defaultValue;
    
    private List<ApiPropertyDTO> children;
}
