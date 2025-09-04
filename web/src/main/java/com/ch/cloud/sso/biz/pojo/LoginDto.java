package com.ch.cloud.sso.biz.pojo;

import com.ch.cloud.upms.dto.LoginUserDto;
import io.swagger.v3.oas.annotations.media.Schema; // 修改: 替换为 Swagger 3.0 的 Schema 注解
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户登录信息") // 修改: 替换 ApiModel 为 Schema
public class LoginDto extends LoginUserDto {

    @Schema(name = "captchaCode", description = "验证码") // 修改: 替换 ApiModelProperty 为 Schema
    private String captchaCode;

    @Schema(name = "captchaVerification", description = "二次验证码") // 修改: 替换 ApiModelProperty 为 Schema
    private String captchaVerification;

}