package com.ch.cloud.sso.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfo {

    private String username;
    private Long userId;
    private Long roleId;

    private Long expireAt;
}
