package com.ch.cloud.sso.biz.pojo;

import com.ch.cloud.upms.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema; // 修改: 替换为 Swagger 3.0 的 Schema 注解
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * decs: 用户信息
 *
 * @author zhimin.ma
 * @since 2019/9/9
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户及权限信息") // 修改: 替换 ApiModel 为 Schema
@JsonIgnoreProperties({"password"})
public class UserVo extends UserDto {

    @Schema(hidden = true) // 修改: 替换 ApiModelProperty 为 Schema
    private String password;

    @Schema(hidden = true) // 修改: 替换 ApiModelProperty 为 Schema
    private String token;

}