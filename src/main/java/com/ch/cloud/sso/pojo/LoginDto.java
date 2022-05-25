package com.ch.cloud.sso.pojo;

import com.ch.cloud.upms.dto.LoginUserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("用户登录信息")
public class LoginDto extends LoginUserDto {

    @ApiModelProperty(name = "captchaCode", value = "验证码", required = true)
    private String captchaCode;

    @ApiModelProperty(name = "captchaVerification", value = "二次验证码")
    private String captchaVerification;

}
