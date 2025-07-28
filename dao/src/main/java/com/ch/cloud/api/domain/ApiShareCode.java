package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ch.cloud.api.dto.ApiResourceDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 接口分享码
 * 
 * @author zhimin.ma
 * @since 2025-07-28
 */
@Data
@TableName(value = "api_share_code", autoResultMap = true)
public class ApiShareCode {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("share_code")
    private String shareCode;

    @TableField("user_id")
    private String userId;

    @TableField(value = "resources", typeHandler = JacksonTypeHandler.class)
    private List<ApiResourceDTO> resources;

    @TableField("expire_time")
    private Date expireTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
