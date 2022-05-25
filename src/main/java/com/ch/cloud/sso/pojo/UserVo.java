package com.ch.cloud.sso.pojo;

import com.ch.cloud.upms.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * decs: 用户信息
 *
 * @author zhimin.ma
 * @date 2019/9/9
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "用户及权限信息", description = "")
@JsonIgnoreProperties({"password"})
public class UserVo extends UserDto {

    @ApiModelProperty(hidden = true)
    private String password;
    @ApiModelProperty(hidden = true)
    private String token;


}
