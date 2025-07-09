package com.ch.cloud.json.enums;

import lombok.Getter;

/**
 * <p>
 * desc: SchemaType
 * </p>
 *
 * @author zhimin
 * @since 2025/6/17 11:37
 */
@Getter
public enum SchemaType {
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    OBJECT("object"),
    ARRAY("array");

    private final String type;

    SchemaType(String type) {
        this.type = type;
    }

    public static SchemaType fromType(String type) {
        for (SchemaType value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return OBJECT;
    }
}
