package com.example.xxljobtest.controller;

import com.example.xxljobtest.config.NacosDataSourceConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "springboot/nacos/config")
public class NacosConfigController {

    @Resource
    private NacosDataSourceConfig nacosDataSourceConfig;

    @GetMapping(path = "get")
    private Map<String, String> getNacosDataSource() {
        Map<String, String> result = new HashMap<>();
        result.put("name", nacosDataSourceConfig.getName());
        result.put("url", nacosDataSourceConfig.getUrl());
        result.put("username", nacosDataSourceConfig.getUsername());
        result.put("password", nacosDataSourceConfig.getPassword());
        result.put("driverClassName", nacosDataSourceConfig.getDriverClassName());
        return result;
    }
}
