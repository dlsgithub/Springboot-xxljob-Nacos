package com.example.xxljobtest.service.jobhandler;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import java.io.IOException;
import java.util.concurrent.Executor;

public class NacosMain {

    public static void main(String[] args) throws NacosException, IOException {
        ConfigService configService = NacosFactory.createConfigService("localhost:8848");
        String context = configService.getConfig("hello-nacos.text", "DEFAULT_GROUP", 3000);
        System.out.println(context);

        // 监听数据变更
        configService.addListener("hello-nacos.text", "DEFAULT_GROUP", new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String s) {
                System.out.println("收到配置变更的通知：" + s);
            }
        });
        System.in.read();
    }
}

