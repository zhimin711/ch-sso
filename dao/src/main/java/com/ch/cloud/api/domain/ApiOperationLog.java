package com.ch.cloud.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("api_operation_log")
public class ApiOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private String userId;

    @TableField("username")
    private String username;

    @TableField("operation_type")
    private String operationType;

    @TableField("operation_content")
    private String operationContent;

    @TableField("ip")
    private String ip;

    @TableField("created_time")
    private Date createdTime;

    @TableField("module")
    private String module;
} 