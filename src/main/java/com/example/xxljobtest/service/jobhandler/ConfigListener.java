package com.example.xxljobtest.service.jobhandler;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import org.springframework.stereotype.Component;

/**
 * @author lixiaoshuang
 */
@Component
public class ConfigListener {

    /**
     * 基于注解监听配置
     *
     * @param newContent
     * @throws Exception
     */
    @NacosConfigListener(dataId = "hello-nacos.text", timeout = 500)
    public void onChange(String newContent) {
        System.out.println("配置变更为 : \n" + newContent);
    }

}

