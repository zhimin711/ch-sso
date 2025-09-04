package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.mapper2.IApiGroupPathMapper;
import com.ch.cloud.api.service.IApiPathService;
import com.ch.cloud.api.service.IApiGroupService;
import com.ch.result.InvokerPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ch.cloud.api.mapper.ApiPathMapper;
import com.ch.cloud.api.domain.ApiPath;

import java.util.List;

/**
 * 接口分组Service业务层
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:35:57
 */
@Service
public class ApiPathServiceImpl extends ServiceImpl<ApiPathMapper, ApiPath> implements IApiPathService {

    @Autowired
    private IApiGroupService apiGroupService;

    @Autowired
    private IApiGroupPathMapper apiGroupPathMapper;

    @Override
    public InvokerPage.Page<ApiPath> pageByGroupId(Long groupId, int pageNum, int pageSize) {
        List<Long> apiIds = apiGroupService.listPathIdsByGroupId(groupId);
        if (apiIds.isEmpty()) {
            return new InvokerPage.Page<ApiPath>();
        }
        Page<ApiPath> page = new Page<>(pageNum, pageSize);
        page = page(page, Wrappers.<ApiPath>lambdaQuery().in(ApiPath::getId, apiIds));
        return InvokerPage.build();
    }

    @Override
    public List<Long> listGroupIds(Long pathId) {
        return apiGroupPathMapper.listGroupIdsByPathIdAndGroupType(pathId, null);
    }
}
