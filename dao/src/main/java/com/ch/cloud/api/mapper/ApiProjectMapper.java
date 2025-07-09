package com.ch.cloud.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.cloud.api.domain.ApiProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 业务-接口项目配置数据服务
 *
 * @author zhimin.ma
 * @since 2022-10-16 11:34:06
 */
@Mapper
public interface ApiProjectMapper extends BaseMapper<ApiProject> {
}
