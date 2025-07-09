package com.ch.cloud.api.enums;

/**
 * <p>
 * desc: ProjectRoleType
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/14
 */
public enum ProjectRoleType {
    MGR,
    DEV,
    TEST,
    OPS,
    VISITOR;
    
    public static ProjectRoleType fromName(String role) {
        for (ProjectRoleType type : values()) {
            if (type.name().equals(role)) {
                return type;
            }
        }
        return VISITOR;
    }
}
