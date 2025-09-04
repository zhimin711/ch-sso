package com.ch.cloud.api.manager;


import com.ch.cloud.api.domain.ApiPath;
import com.ch.cloud.api.dto.ApiGroupPathDetailDTO;
import com.ch.result.InvokerPage;

import java.util.List;

public interface ApiPathManager {
    InvokerPage.Page<ApiPath> pageByGroupId(Long groupId, int pageNum, int pageSize);

    Boolean save(ApiGroupPathDetailDTO record);

    List<Long> listGroupIds(Long pathId);
    
    Boolean update(ApiGroupPathDetailDTO record);
    
    Boolean delete(Long id);
}
