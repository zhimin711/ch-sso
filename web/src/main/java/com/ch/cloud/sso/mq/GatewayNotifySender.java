package com.ch.cloud.sso.mq;

import com.alibaba.fastjson.JSON;
import com.ch.pojo.KeyValue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/25 08:46
 */
@Component
public class GatewayNotifySender {

    @Value("${rocketmq.enabled:false}")
    private Boolean mqOn;

    public static final String MQ_GATEWAY_CLEAN_TOPIC = "gateway-clean";

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    public void cleanNotify(KeyValue keyValue){
        if(!mqOn) return;
        String message = JSON.toJSONString(keyValue);
        rocketMQTemplate.convertAndSend(MQ_GATEWAY_CLEAN_TOPIC, message);
    }

}
