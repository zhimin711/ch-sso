package com.ch.cloud.api.pojo;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/11/15
 */
@Data
public class DocParameter {
    
    private String in;
    
    private String name;
    
    private String description;
    
    private boolean required;
    
    private String type;
    
    private String format;
    
    //    @JSONField(name = "x-example")
    private String example;
    
    private JSONObject schema;
}
