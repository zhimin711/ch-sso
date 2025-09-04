package com.ch.cloud.api.manager;

import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.dto.ApiResourceDTO;
import com.ch.cloud.api.pojo.GroupPath;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * desc: IApiGroupManage
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/11/3
 */
public interface ApiGroupManager {

    List<GroupPath> convertTree(List<ApiGroup> apiGroups, Boolean withApi);

    void parseDocTags(Long projectId, JSONObject apiDocJson);

    void parsePathAndTag(Long projectId, JSONObject obj);

    void parseDocDefinitions(Long projectId, JSONObject apiDocJson);

    void parseDocSchemas(Long projectId, JSONObject apiDocJson);

    boolean createDefaultGroup(Long id);

    List<GroupPath> convertTree(List<ApiGroup> apiGroups, Map<Long, ApiResourceDTO> resourceMap);

    Boolean delete(Long id);
}
