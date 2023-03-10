package com.example.xxljobtest.controller;


import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "springboot/nacos/config")
public class AnnotationGetController {

    @NacosValue(value = "${name:}", autoRefreshed = true)
    private String name;

    @NacosValue(value = "${url:}", autoRefreshed = true)
    private String url;

    @NacosValue(value = "${username:}", autoRefreshed = true)
    private String username;

    @NacosValue(value = "${password:}", autoRefreshed = true)
    private String password;

    @NacosValue(value = "${driverClassName:}", autoRefreshed = true)
    private String driverClassName;

    @GetMapping(path = "annotation/get1")
    private Map<String, String> getNacosDataSource2() {
        Map<String, String> result = new HashMap<>();
        result.put("name", name);
        result.put("url", url);
        result.put("username", username);
        result.put("password", password);
        result.put("driverClassName", driverClassName);
        return result;
    }
}
