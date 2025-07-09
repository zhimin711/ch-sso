package com.ch.cloud.api.enums;

import lombok.Getter;

/**
 * <p>
 * desc: GroupType
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/11/3
 */
@Getter
public enum GroupType {
    // 模块分组
    MODULE("1"),
    // 标签分组
    TAG("2"),
    // 自定义分组
    CUSTOM("3");
    
    private final String code;
    
    GroupType(String code) {
        this.code = code;
    }

    public static GroupType fromCode(String code) {
        for (GroupType value : GroupType.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return CUSTOM;
    }
}
