package com.ch.cloud.sso.biz.pojo;

import com.ch.cloud.upms.dto.RoleDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * decs:
 *
 * @author 01370603
 * @since 2019/9/9
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RoleVo extends RoleDto {

    public RoleVo(Long id, String code, String name) {
        super.setId(id);
        super.setCode(code);
        super.setName(name);
    }

}
