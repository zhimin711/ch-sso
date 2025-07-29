package com.ch.cloud.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.cloud.api.domain.ApiShareCode;
import com.ch.cloud.api.dto.ApiShareCodeQueryDTO;

/**
 * 接口分享码服务接口
 * 
 * @author zhimin.ma
 * @since 2025-07-28
 */
public interface IApiShareCodeService extends IService<ApiShareCode> {
    
    /**
     * 分页查询接口分享码
     * 
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    IPage<ApiShareCode> pageByQuery(ApiShareCodeQueryDTO queryDTO);
}
