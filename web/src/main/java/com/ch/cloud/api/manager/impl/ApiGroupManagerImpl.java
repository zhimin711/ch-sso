package com.ch.cloud.api.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.api.domain.ApiGroup;
import com.ch.cloud.api.domain.ApiPath;
import com.ch.cloud.api.domain.ApiSchema;
import com.ch.cloud.api.dto.ApiGroupPathDTO;
import com.ch.cloud.api.dto.ApiResourceDTO;
import com.ch.cloud.api.enums.GroupType;
import com.ch.cloud.api.manager.ApiGroupManager;
import com.ch.cloud.api.pojo.DocParameter;
import com.ch.cloud.api.pojo.GroupPath;
import com.ch.cloud.api.service.IApiGroupService;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.cloud.api.service.IApiSchemaService;
import com.ch.cloud.api.utils.ApiSchemaConverter;
import com.ch.cloud.api.utils.ApiUtil;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * desc: ApiGroupManageImpl
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/11/3
 */
@Service
public class ApiGroupManagerImpl implements ApiGroupManager {

    @Autowired
    private IApiGroupService apiGroupService;

    @Autowired
    private IApiPathService apiPathService;

    @Autowired
    private IApiSchemaService apiSchemaService;

    @Override
    public List<GroupPath> convertTree(List<ApiGroup> apiGroups, Boolean withApi) {
        if (CommonUtils.isEmpty(apiGroups)) {
            return Lists.newArrayList();
        }
        
        // 构建分组树
        Map<Long, List<GroupPath>> groupMap = apiGroups.stream().map(e -> {
            GroupPath dto = BeanUtil.copyProperties(e, GroupPath.class);
            dto.setName(e.getAlias());
            dto.setKey("group-" + e.getId());
            dto.setType(Integer.parseInt(e.getType()));
            return dto;
        }).collect(Collectors.groupingBy(GroupPath::getParentId));
        List<Long> groupIds = apiGroups.stream().map(ApiGroup::getId).collect(Collectors.toList());

        List<ApiGroupPathDTO> groupPathList =
                withApi ? apiGroupService.listPathIdsByGroupIds(groupIds) : Lists.newArrayList();
        Map<Long, List<ApiGroupPathDTO>> groupPathMap = groupPathList.stream()
                .collect(Collectors.groupingBy(ApiGroupPathDTO::getGroupId));
        groupMap.forEach((k, v) -> {
            v.forEach(e -> {
                if (groupPathMap.containsKey(e.getId())) {
                    List<Long> pathIds = groupPathMap.get(e.getId()).stream().map(ApiGroupPathDTO::getPathId)
                            .collect(Collectors.toList());
                    e.setChildren(convertPathList(e.getKey(), pathIds));
                }
                if (groupMap.containsKey(e.getId())) {
                    e.setChildren(groupMap.get(e.getId()));
                }
            });
        });

        return groupMap.get(0L);
    }

    private List<GroupPath> convertPathList(String groupKey, List<Long> pathIds) {
        if (CommonUtils.isEmpty(pathIds)) {
            return null;
        }
        List<ApiPath> apis = apiPathService.listByIds(pathIds);
        if (apis.isEmpty()) {
            return null;
        }
        return apis.stream().map(api -> {
            GroupPath dto = BeanUtil.copyProperties(api, GroupPath.class);
            dto.setMethod(api.getMethod().toUpperCase());
            dto.setKey(groupKey + ":path-" + api.getId());
            dto.setType(-1);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void parseDocTags(Long projectId, JSONObject apiDocJson) {
        if (!apiDocJson.containsKey("tags")) {
            return;
        }
        // swagger parse
        List<ApiGroup> groups = apiDocJson.getJSONArray("tags").toJavaList(ApiGroup.class);
        List<String> nameList = groups.stream().map(ApiGroup::getName).collect(Collectors.toList());
        apiGroupService.lambdaUpdate().eq(ApiGroup::getProjectId, projectId).remove();
        List<ApiGroup> list = apiGroupService.lambdaQuery().eq(ApiGroup::getProjectId, projectId)
                .in(ApiGroup::getName, nameList).list();
        List<ApiGroup> newList = Lists.newArrayList();
        List<ApiGroup> updateList = Lists.newArrayList();
        AtomicInteger sort = new AtomicInteger(list.isEmpty() ? 0 : list.get(list.size() - 1).getSort());
        //没有历史的接口分级数据
        if (CommonUtils.isEmpty(list)) {
            newList.addAll(groups);
        } else {
            Map<String, ApiGroup> groupMap = groups.stream()
                    .collect(Collectors.toMap(ApiGroup::getName, Function.identity()));

            list.forEach(group -> {
                ApiGroup apiGroup = groupMap.get(group.getName());

                GroupType groupType = GroupType.fromCode(group.getType());

                if (groupType == GroupType.MODULE) {
                    if (!CommonUtils.isEquals(group.getDescription(), apiGroup.getDescription())) {
                        group.setDescription(apiGroup.getDescription());
                        updateList.add(group);
                    }
                } else if (groupType == GroupType.TAG) {
                    group.setType(GroupType.MODULE.getCode());
                    group.setDescription(apiGroup.getDescription());
                    group.setSort(sort.addAndGet(1));
                    updateList.add(group);
                }

                groupMap.remove(group.getName());
            });
            if (CommonUtils.isNotEmpty(groupMap)) {
                newList.addAll(groupMap.values());
            }
        }
        boolean isName;
        if (apiDocJson.containsKey("openapi")) {
            double openapiVersion = apiDocJson.getDoubleValue("openapi");
            isName = !(openapiVersion > 3.0);
        } else {
            isName = true;
        }
        if (!newList.isEmpty()) {
            newList.forEach(group -> {
                group.setProjectId(projectId);
                group.setParentId(0L);
                group.setType(GroupType.MODULE.getCode());
                group.setSort(sort.addAndGet(1));
                if (isName) {
                    group.setAlias(group.getName());
                } else {
                    group.setAlias(group.getDescription());
                }
            });
            apiGroupService.saveBatch(newList);
        }
        if (CommonUtils.isNotEmpty(updateList)) {
            apiGroupService.updateBatchById(updateList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void parsePathAndTag(Long projectId, JSONObject obj) {
        if (!obj.containsKey("paths")) {
            return;
        }
        List<ApiSchema> schemas = apiSchemaService.lambdaQuery().eq(ApiSchema::getProjectId, projectId).list();
        Map<String, ApiSchema> schemaMap = schemas.stream()
                .collect(Collectors.toMap(ApiSchema::getDefKey, Function.identity()));
        JSONObject pathObjs = obj.getJSONObject("paths");
        Set<String> paths = pathObjs.keySet();
        for (String path : paths) {
            JSONObject methodObjs = pathObjs.getJSONObject(path);

            Set<String> methods = methodObjs.keySet();
            for (String method : methods) {
                JSONObject apiObj = methodObjs.getJSONObject(method);
                ApiPath apiPath = apiPathService.lambdaQuery().eq(ApiPath::getProjectId, projectId)
                        .eq(ApiPath::getPath, path).eq(ApiPath::getMethod, method).one();
                boolean exists = apiPath != null;
                if (apiPath == null) {
                    apiPath = new ApiPath();
                    apiPath.setProjectId(projectId);
                    apiPath.setPath(path);
                    apiPath.setMethod(method);
                }
                apiPath.setImportContent(apiObj.toJSONString());
                apiPath.setName(apiObj.getString("summary"));
                apiPath.setDescription(apiObj.getString("description"));
                if (CommonUtils.isEmpty(apiPath.getName()) && CommonUtils.isEmpty(apiPath.getDescription())) {
                    if (apiObj.containsKey("operationId")) {
                        apiPath.setName(apiObj.getString("operationId"));
                    }
                }

                if (apiObj.containsKey("consumes")) {
                    apiPath.setConsumes(apiObj.getJSONArray("consumes").toString());
                }
                if (apiObj.containsKey("produces")) {
                    apiPath.setProduces(apiObj.getJSONArray("produces").toString());
                }
                if (apiObj.containsKey("parameters")) {
                    // 该参数，包含path、query、header、cookie
                    List<DocParameter> parameters = apiObj.getList("parameters", DocParameter.class);
                    String jsonParams = ApiSchemaConverter.parsePathParams2ApiParams(parameters, schemaMap);
                    apiPath.setParameters(jsonParams);
                    Optional<DocParameter> bodyOpt = parameters.stream().filter(param -> param.getIn().equals("body"))
                            .findFirst();
                    if (bodyOpt.isPresent()) {
                        String jsonSchema = ApiSchemaConverter.parseParamBody2JsonSchema(bodyOpt.get(), schemaMap);
                        apiPath.setBody(jsonSchema);
                    }
                }
                if (apiObj.containsKey("requestBody")) {
                    JSONObject requestBody = apiObj.getJSONObject("requestBody");
                    String jsonSchema = ApiSchemaConverter.parseBody2JsonSchema(requestBody, schemaMap);
                    apiPath.setBody(jsonSchema);
                }
                if (apiObj.containsKey("responses")) {
                    JSONObject responses = apiObj.getJSONObject("responses");
                    String jsonSchema = ApiSchemaConverter.parseResponses2JsonSchema(responses, schemaMap);
                    apiPath.setResponses(jsonSchema);
                }

                apiPath.setImportAt(DateUtils.current());
                if (!exists) {
                    apiPath.setIsImport(true);
                    apiPathService.save(apiPath);
                } else {
                    // 判断是否有更新内容
                    //                    if (!CommonUtils.isEquals(apiPath.getImportContent(), apiObj.toJSONString())) {
                    apiPathService.updateById(apiPath);
                    //                    }
                }
                // 添加标签
                List<String> tags = apiObj.getJSONArray("tags").toList(String.class);
                List<ApiGroup> groupList = apiGroupService.lambdaQuery().in(ApiGroup::getName, tags)
                        .eq(ApiGroup::getProjectId, projectId).eq(ApiGroup::getParentId, 0L).list();
                List<String> newTags = Lists.newArrayList(tags);

                ApiPath finalPath = apiPath;
                ApiGroup apiGroup = apiGroupService.getByPathId(apiPath.getId());
                if (CommonUtils.isNotEmpty(groupList)) {
                    Optional<ApiGroup> firstGroupOpt = groupList.stream().filter(group -> {
                        GroupType groupType = GroupType.fromCode(group.getType());
                        return groupType == GroupType.MODULE;
                    }).findFirst();
                    // 存在接口分组
                    if (firstGroupOpt.isPresent()) {
                        ApiGroup firstGroup = firstGroupOpt.get();
                        // 若原来的接口分组与新的接口分组名称不一致，则删除
                        if (apiGroup != null && !firstGroup.getName().equals(apiGroup.getName())) {
                            apiGroupService.removeGroupPath(apiGroup.getId(), apiPath.getId());
                        }
                        apiGroupService.addGroupPath(firstGroup.getId(), apiPath.getId());
                        // 删除默认接口分组,保留标签分组
                        newTags.removeIf(e -> firstGroup.getName().equals(e));
                    } else {
                        // 绑定默认的接口分组
                        ApiGroup firstGroup = apiGroupService.lambdaQuery().eq(ApiGroup::getProjectId, projectId)
                                .eq(ApiGroup::getName, ApiUtil.API_GROUP_DEFAULT)
                                .eq(ApiGroup::getType, GroupType.MODULE.getCode()).one();
                        if (firstGroup != null) {
                            apiGroupService.addGroupPath(firstGroup.getId(), apiPath.getId());
                        }
                    }
                }
                if (CommonUtils.isEmpty(newTags)) {
                    continue;
                }
                // 添加标签分组
                Map<String, Long> tagGroupIdMap = groupList.stream().filter(group -> {
                    GroupType groupType = GroupType.fromCode(group.getType());
                    return groupType == GroupType.TAG;
                }).collect(Collectors.toMap(ApiGroup::getName, ApiGroup::getId));

                newTags.forEach(e -> {
                    if (!tagGroupIdMap.containsKey(e)) {
                        ApiGroup tagGroup = new ApiGroup();
                        tagGroup.setParentId(0L);
                        tagGroup.setProjectId(projectId);
                        tagGroup.setType(GroupType.TAG.getCode());
                        tagGroup.setAlias(e);
                        tagGroup.setName(e);
                        apiGroupService.save(tagGroup);
                        apiGroupService.addGroupPath(tagGroup.getId(), finalPath.getId());
                    } else {
                        Long groupId = tagGroupIdMap.get(e);
                        if (!apiGroupService.existsGroupPath(groupId, finalPath.getId())) {
                            apiGroupService.addGroupPath(groupId, finalPath.getId());
                        }
                    }

                });
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void parseDocDefinitions(Long projectId, JSONObject apiDocJson) {
        if (!apiDocJson.containsKey("definitions")) {
            return;
        }
        JSONObject definitionObjs = apiDocJson.getJSONObject("definitions");
        Set<String> definitionKeys = definitionObjs.keySet();
        for (String key : definitionKeys) {
            JSONObject definitionObj = definitionObjs.getJSONObject(key);
            ApiSchema schema = apiSchemaService.lambdaQuery().eq(ApiSchema::getDefKey, key)
                    .eq(ApiSchema::getProjectId, projectId).one();
            if (schema == null) {
                schema = new ApiSchema();
                schema.setProjectId(projectId);
                schema.setDefKey(key);

                schema.setCreateBy(ContextUtil.getUserId());
                schema.setCreateAt(DateUtils.current());
            }

            schema.setType(definitionObj.getString("type"));
            schema.setTitle(definitionObj.getString("title"));
            schema.setDescription(definitionObj.getString("description"));
            if (CommonUtils.isEmpty(schema.getTitle()) && CommonUtils.isNotEmpty(schema.getDescription())) {
                schema.setTitle(schema.getDescription());
            }

            if (definitionObj.containsKey("required")) {
                schema.setRequired(definitionObj.getJSONArray("required").toString());
            }
            if (definitionObj.containsKey("properties")) {
                schema.setProperties(definitionObj.getJSONObject("properties").toString());
            }
            if (schema.getId() == null) {
                apiSchemaService.save(schema);
            } else {
                apiSchemaService.updateById(schema);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void parseDocSchemas(Long projectId, JSONObject apiDocJson) {
        if (!apiDocJson.containsKey("components")) {
            return;
        }
        JSONObject componentsObj = apiDocJson.getJSONObject("components");
        if (!componentsObj.containsKey("schemas")) {
            return;
        }
        JSONObject schemasObj = componentsObj.getJSONObject("schemas");
        Set<String> schemaKeys = schemasObj.keySet();
        List<ApiSchema> schemas = apiSchemaService.lambdaQuery().eq(ApiSchema::getProjectId, projectId)
                .in(ApiSchema::getDefKey, schemaKeys).list();
        Map<String, ApiSchema> schemaMap = schemas.stream()
                .collect(Collectors.toMap(ApiSchema::getDefKey, Function.identity()));
        for (String key : schemaKeys) {
            JSONObject schemaObj = schemasObj.getJSONObject(key);
            ApiSchema schema = new ApiSchema();
            schema.setProjectId(projectId);
            schema.setDefKey(key);
            schema.setType(schemaObj.getString("type"));
            schema.setTitle(schemaObj.getString("title"));
            schema.setDescription(schemaObj.getString("description"));
            if (schemaObj.containsKey("required")) {
                schema.setRequired(schemaObj.getJSONArray("required").toString());
            }
            if (schemaObj.containsKey("properties")) {
                schema.setProperties(schemaObj.getJSONObject("properties").toString());
            }
            if (!schemaMap.containsKey(key)) {
                apiSchemaService.save(schema);
            } else {
                ApiSchema apiSchema = schemaMap.get(key);
                if (apiSchema.equals(schema)) {
                    continue;
                }
                schema.setId(apiSchema.getId());
                apiSchemaService.updateById(schema);
            }
        }
    }

    @Override
    public boolean createDefaultGroup(Long projectId) {
        ApiGroup group = new ApiGroup();
        group.setProjectId(projectId);
        group.setParentId(0L);
        group.setType(GroupType.MODULE.getCode());
        group.setName(ApiUtil.API_GROUP_DEFAULT);
        group.setDescription("默认");
        group.setSort(0);
        return apiGroupService.save(group);
    }

    @Override
    public List<GroupPath> convertTree(List<ApiGroup> apiGroups, Map<Long, ApiResourceDTO> resourceMap) {
        if (CommonUtils.isEmpty(apiGroups)) {
            return Lists.newArrayList();
        }
        // 构建分组树
        Map<Long, List<GroupPath>> groupMap = apiGroups.stream().map(e -> {
            GroupPath dto = BeanUtil.copyProperties(e, GroupPath.class);
            dto.setKey("group-" + e.getId());
            return dto;
        }).collect(Collectors.groupingBy(GroupPath::getParentId));
        groupMap.forEach((k, v) -> {
            v.forEach(e -> {
                if (resourceMap.containsKey(e.getId())) {
                    e.setChildren(convertPathList(e.getKey(), resourceMap.get(e.getId()).getApiIds()));
                }
                if (groupMap.containsKey(e.getId())) {
                    e.setChildren(groupMap.get(e.getId()));
                }
            });
        });
        return groupMap.get(0L);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean delete(Long id) {
        ApiGroup apiGroup = apiGroupService.getById(id);
        Assert.notNull(apiGroup, PubError.NOT_EXISTS, id);
        Assert.isFalse(CommonUtils.isEquals(apiGroup.getName(), ApiUtil.API_GROUP_DEFAULT),PubError.NOT_ALLOWED,"默认分组删除");

        boolean ok = apiGroupService.removeById(id);
        List<Long> ids = apiGroupService.listPathIdsByGroupId(id);
        if (CommonUtils.isNotEmpty(ids)) {
            apiPathService.removeByIds(ids);
            ids.forEach(e -> apiGroupService.removeGroupPath(id, e));
        }
        return ok;
    }
}
