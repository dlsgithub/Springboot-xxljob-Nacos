package com.example.xxljobtest.config;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.Data;
import org.springframework.stereotype.Component;

@NacosConfigurationProperties(prefix = "spring.datasource", dataId = "nacos-datasource.yaml", autoRefreshed = true)
@Component
@Data
public class NacosDataSourceConfig {

    private String name;

    private String url;

    private String username;

    private String password;

    private String driverClassName;
}

