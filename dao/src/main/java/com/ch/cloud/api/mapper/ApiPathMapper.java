package com.ch.cloud.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.cloud.api.domain.ApiPath;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口分组数据服务
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:35:57
 */
@Mapper
public interface ApiPathMapper extends BaseMapper<ApiPath> {
}
