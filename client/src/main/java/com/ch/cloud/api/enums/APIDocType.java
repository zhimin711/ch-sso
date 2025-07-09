package com.ch.cloud.api.enums;

import lombok.Getter;

@Getter
public enum APIDocType {
    OPENAPI("openapi"),
    SWAGGER("swagger"),
    ;
    private final String value;
    APIDocType(String value) {
        this.value = value;
    }

    public static APIDocType fromValue(String apiDocType) {
        for (APIDocType type : APIDocType.values()) {
            if (type.value.equals(apiDocType)) {
                return type;
            }
        }
        return null;
    }
}
