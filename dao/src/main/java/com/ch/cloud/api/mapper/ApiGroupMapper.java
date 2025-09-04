package com.ch.cloud.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.cloud.api.domain.ApiGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口分组数据服务
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:31:22
 */
@Mapper
public interface ApiGroupMapper extends BaseMapper<ApiGroup> {
}
