package com.ch.cloud.sso.pojo;

import com.ch.cloud.client.dto.RoleDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/9/9
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
