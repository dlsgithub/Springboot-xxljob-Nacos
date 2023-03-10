# 三、Springboot获取Nacos上的动态配置

本文介绍下如何在 Spring Boot 项目中使用 Nacos，Nacos 主要分为两个部分，配置中心和服务注册与发现。在使用 Spring Boot 项目中使用 Nacos ，首先要保证启动一个 Nacos 服务，具体可以参考【快速上手 Nacos】来搭建一个单机的 Nacos 服务。

配置中心
创建配置
打开控制台 http://127.0.0.1:8848/nacos ，进入 配置管理-配置列表 点击+号新建配置，这里创建个数据源配置例子: nacos-datasource.yaml
```java
spring:
    datasource:
     name: datasource
     url: jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useDynamicCharsetInfo=false&useSSL=false
     username: root
     password: root
     driverClassName: com.mysql.jdbc.Driver
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/3e33c5880e8e4029899d92c706fa0ad6.png)
添加依赖
配置创建好就可以在控制台 配置管理-配置列表中查看到。接下来演示下怎么在 Spring Boot 项目中获取到 Nacos 中的配置信息。

需要在项目中添加以下依赖:
```java
<!--配置中心 starter-->
<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-config-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```
然后在项目中的 application.properties 文件中添加 nacos 的一些配置：
```java
nacos.config.server-addr=127.0.0.1:8848
nacos.config.group=DEFAULT_GROUP
nacos.config.namespace=
nacos.config.username=nacos
nacos.config.password=nacos
```

## 获取配置

### 绑定到类获取

可以通过 @NacosConfigurationProperties 注解将 nacos-datasource.yaml 中的配置绑定到 NacosDataSourceConfig 类上。这样就可以通过 @Resource 或 @Autowired 将 NacosDataSourceConfig 注入到要使用的地方。
```java
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
```
创建一个 Controller ，写一个获取配置信息的接口：
```java
/**
 * @author lixiaoshuang
 */
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
```
然后启动服务，访问 http://localhost:8080/springboot/nacos/config/get就可以获取到对应的配置信息:
![在这里插入图片描述](https://img-blog.csdnimg.cn/672ded4a11a74ce28cccb2baeab9be11.png)


### @NacosValue+@NacosPropertySource 注解获取
在创建一个 properties 格式的配置，演示下使用 @NacosValue + @NacosPropertySource 注解获取配置信息。还是打开 配置管理-配置列表 点击+号新建配置：nacos-datasource.properties
```java
name=datasource
url=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useDynamicCharsetInfo=false&useSSL=false
username=root
password=root
driverClassName=com.mysql.jdbc.Driver
```
![https://tva1.sinaimg.cn/large/e6c9d24ely1h3kounqdmsj216m0u0wgp.jpg](https://img-blog.csdnimg.cn/d690a909ffa04b94a780ef9942d7a711.png)
通过 @NacosValue + @NacosPropertySource 注解获取指定 dataId 的配置
```java
@RestController
@RequestMapping(path = "springboot/nacos/config")
@NacosPropertySource(dataId = "nacos-datasource.properties", autoRefreshed = true)
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
    
    @GetMapping(path = "annotation/get")
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
```
访问 http://localhost:8080/springboot/nacos/config/annotation/get 获取 nacos-datasource.properties 的配置信息：
![在这里插入图片描述](https://img-blog.csdnimg.cn/cf295f34720642379229d2a3a26a6f23.png)
或者启动类添加注解

dataId 对应唯一值，autoRefreshed开启自动刷新。
![在这里插入图片描述](https://img-blog.csdnimg.cn/eec0b5874078437fbc5254e4652d56d9.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/4de92db0eb5742e49727726f55c712f9.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/d151a892f9e7400d9f4b19a398340c56.png)

## 配置监听

Spring Boot 的使用方式也可以通过 @NacosConfigListener 注解进行配置变更的监听，在创建一个 hello-nacos.text 配置：
![https://tva1.sinaimg.cn/large/e6c9d24ely1h3kpuvham9j219k0u0dhh.jpg](https://img-blog.csdnimg.cn/42dd2f5c8d0046a69698196e5c5e65fc.png)

```java
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
```
然后在将 hello-nacos.text 的配置内容修改为 ” hello nacos config “，代码中就会回调 onChange() 方法
![!\[https://tva1.sinaimg.cn/large/e6c9d24ely1h3kpz4de68j210q0gomy4.jpg\](https://img-blog.csdnimg.cn/025128cbe4234b1dbb13a2db032d8f0b.png](https://img-blog.csdnimg.cn/d90eb369504743d59871aa8c023c87d3.png)
或者![在这里插入图片描述](https://img-blog.csdnimg.cn/cb31a2a8545946c983ab2510b7ec9610.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/3ac9f676c8284cddb5fae566f79d8d5b.png)
将yml文件中xxl-job-admin的配置全放到nacos中
![在这里插入图片描述](https://img-blog.csdnimg.cn/fa872eea4d3f497c95088fe97bc6f378.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/6f364268941348ec851c96006c03154d.png)
修改dataId
![在这里插入图片描述](https://img-blog.csdnimg.cn/1611b1245e2b435ba804f56f83128098.png)
重新启动测试程序，再启动定时任务，成功
![在这里插入图片描述](https://img-blog.csdnimg.cn/dabd64fa1e034adda04943c69feda65f.png)
至此，完成xxl-job-admin将配置信息放到nacos中![在这里插入图片描述](https://img-blog.csdnimg.cn/02a83e7030cd4aac93a7664b655ea1bf.png)
Springboot项目中将配置的xxl-job信息放到Nacos中，项目中只保留Nacos的地址等信息。
![在这里插入图片描述](https://img-blog.csdnimg.cn/af4989bc60e7490fb5351db586b23fdb.png)



*********《*以下未做验证，代码中没有展示*》*********

## 服务注册&发现

### 服务注册

在项目中添加以下依赖：
```java
<!--注册中心 starter-->
<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-discovery-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```
然后在项目中的 application.properties 文件中添加 nacos 的一些配置：
```java
nacos.discovery.server-addr=127.0.0.1:8848
nacos.discovery.auto-register=true
nacos.discovery.register.clusterName=SPRINGBOOT
nacos.discovery.username=nacos
nacos.discovery.password=nacos
```

当添加完配置以后，并且开启了自动注册，启动服务以后看到下面这段日志，就说明服务注册成功了。

```java
Finished auto register service : SPRING_BOOT_SERVICE, ip : 192.168.1.8, port : 8222
```
服务发现
可以使用 Nacos 提供的 NacosNamingService 来获取到服务的实例，可以通过 @NacosInjected 注解将 NacosNamingService 注入到需要使用的地方。
```java
/**
 * @author lixiaoshuang
 */
@RestController
@RequestMapping(path = "springboot/nacos/discovery")
public class NacosDiscoveryController {
    
    @NacosInjected
    private NacosNamingService nacosNamingService;
    
    @RequestMapping(path = "get")
    public List<Instance> getInfo(@RequestParam("serviceName") String serviceName) throws NacosException {
        return nacosNamingService.getAllInstances(serviceName);
    }
}
```
通过调用 http://localhost:8222/springboot/nacos/discovery/get?serviceName=SPRING_BOOT_SERVICE 获取 SPRING_BOOT_SERVICE 服务的实例信息。
![在这里插入图片描述](https://img-blog.csdnimg.cn/10ae4bd01cdf4b5abbf7ac78661f0d94.png)

项目测试代码地址：[https://github.com/dlsgithub/Springboot-xxljob-Nacos](https://github.com/dlsgithub/Springboot-xxljob-Nacos)

参考文章：[https://blog.csdn.net/qq_34264849/article/details/125497423?ops_request_misc=&request_id=&biz_id=102&utm_term=springboot%E7%9B%B4%E6%8E%A5%E4%BB%8Enacos%E8%AE%A2%E9%98%85%E6%9C%8D%E5%8A%A1&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-3-125497423.142^v73^wechat,201^v4^add_ask,239^v2^insert_chatgpt&spm=1018.2226.3001.4187](https://blog.csdn.net/qq_34264849/article/details/125497423?ops_request_misc=&request_id=&biz_id=102&utm_term=springboot%E7%9B%B4%E6%8E%A5%E4%BB%8Enacos%E8%AE%A2%E9%98%85%E6%9C%8D%E5%8A%A1&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-3-125497423.142%5Ev73%5Ewechat,201%5Ev4%5Eadd_ask,239%5Ev2%5Einsert_chatgpt&spm=1018.2226.3001.4187)
