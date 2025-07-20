package com.ch.cloud.api.manager;

import com.ch.cloud.api.dto.ApiResourceDTO;

import java.util.List;

public interface ApiShareManager {
    String createShareCode(List<ApiResourceDTO> resources);

    List<ApiResourceDTO> getResources(String shareCode);

}
