package com.ch.cloud.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.cloud.api.domain.ApiSchema;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口对象定义信息数据服务
 *
 * @author zhimin.ma
 * @since 2022-10-16 18:03:21
 */
@Mapper
public interface ApiSchemaMapper extends BaseMapper<ApiSchema> {
}
