package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import com.ch.cloud.api.dto.ApiResourceDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("api_share_code")
public class ApiShareCode {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("share_code")
    private String shareCode;

    @TableField("user_id")
    private String userId;

    @TableField(value = "resources", typeHandler = Fastjson2TypeHandler.class)
    private List<ApiResourceDTO> resources;

    @TableField("expire_time")
    private Date expireTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
