package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiShareCode;
import com.ch.cloud.api.dto.ApiShareCodeQueryDTO;
import com.ch.cloud.api.mapper.ApiShareCodeMapper;
import com.ch.cloud.api.service.IApiShareCodeService;
import com.ch.utils.CommonUtils;
import org.springframework.stereotype.Service;

/**
 * 接口分享码服务实现类
 * 
 * @author zhimin.ma
 * @since 2025-07-28
 */
@Service
public class ApiShareCodeServiceImpl extends ServiceImpl<ApiShareCodeMapper, ApiShareCode> implements IApiShareCodeService {
    
    @Override
    public IPage<ApiShareCode> pageByQuery(ApiShareCodeQueryDTO queryDTO) {
        Page<ApiShareCode> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryChainWrapper<ApiShareCode> queryWrapper = lambdaQuery().eq(ApiShareCode::getProjectId, queryDTO.getProjectId());
        
        // 根据用户ID查询
        if (CommonUtils.isNotEmpty(queryDTO.getUserId())) {
            queryWrapper.eq(ApiShareCode::getUserId, queryDTO.getUserId());
        }
        
        // 根据分享码查询
        if (CommonUtils.isNotEmpty(queryDTO.getShareCode())) {
            queryWrapper.like(ApiShareCode::getShareCode, queryDTO.getShareCode());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(ApiShareCode::getCreateTime);
        
        return queryWrapper.page(page);
    }
}