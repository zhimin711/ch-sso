package com.ch.cloud.api.dto;

import lombok.Data;

@Data
public class EnvDTO {

    private Long id;

    private String name;

    private String domain;

    private String prefix;
}
