package com.ch.cloud.api.dto;


import lombok.Data;

import java.util.List;

@Data
public class ApiResourceDTO {

    private Long groupId;

    private List<Long> apiIds;
}
