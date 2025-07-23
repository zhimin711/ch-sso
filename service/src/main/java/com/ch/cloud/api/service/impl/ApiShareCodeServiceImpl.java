package com.ch.cloud.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.cloud.api.domain.ApiShareCode;
import com.ch.cloud.api.mapper.ApiShareCodeMapper;
import com.ch.cloud.api.service.IApiShareCodeService;
import org.springframework.stereotype.Service;

@Service
public class ApiShareCodeServiceImpl extends ServiceImpl<ApiShareCodeMapper, ApiShareCode> implements IApiShareCodeService {
}