package com.example.xxljobtest;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@NacosPropertySource(dataId = "application.yml", autoRefreshed = true)
public class XxljobTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(XxljobTestApplication.class, args);
    }

}
