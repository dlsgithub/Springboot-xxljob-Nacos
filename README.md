# Springboot+xxljob+nacos项目

## 一：Springboot集成xxljob
一、xxl-job简介
Xxl-job项目地址：
GitHub地址：https://github.com/xuxueli/xxl-job
Gitee地址：https://gitee.com/xuxueli0323/xxl-job
二、Xxl-job使用
2.1. 下载项目
通过上面地址下载项目源码，用IDEA打开项目，配置好Maven，并刷新Maven加载项目相关依赖。
2.2. Install项目
我们把项目install成maven依赖到本地maven仓库。防止后期我们把xxl-job-admin拿出来单独导入IDEA时，使用xxl-job-core这依赖时报错，因为阿里maven库是没有这个包的，要自己install一下。
![在这里插入图片描述](https://img-blog.csdnimg.cn/c6a802382cc44ea09f835fe3ee657520.png)
2.3 xxl-job-admin构建
xxl-job-admin 本身就是一个 springboot 项目，将 xxl-job-admin 这个子项目从源代码中复制出来，使用 idea 打开，配置好 Maven，刷新Maven导入依赖（xxl-job-core等。上面执行 install 步骤后，这里刷新Maven就不会报错）。这个就是我们的定时任务管理器（调度中心），将项目中 application.properties 配置文件中的 server.port 改一个非8080的不常用的端口（9090），然后将数据库连接信息改成 xxl-job 的实际数据库连接地址。然后将xxl-job-admin项目打成jar包，执行命令 java -jar xxl-job-admin-2.4.0-SNAPSHOT.jar 独立于自己的真实业务项目运行。
【  创建xxl-job-admin数据库 -- 修改配置文件 --  构建 xxl-job-admin   -- 启动xxl-job-admin项目 -- 访问xxl-job-admin 】
![在这里插入图片描述](https://img-blog.csdnimg.cn/fbe490e163a642c09e770db3cee2bb70.png)
![!\[在这里插入图片描述\](https://img-blog.csdnimg.cn/567602d7d15c43cf8f921c203d226948.png](https://img-blog.csdnimg.cn/caeeaaeade11479cbd4bfa2e77f83be3.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/bc20a90573ab47c482d0c062b1ce7bd9.png)
将xxl-job-admin-2.4.0-SNAPSHOT.jar 放到路径为英文的文件夹，打开cmd在该目录执行java -jar xxl-job-admin-2.4.0-SNAPSHOT.jar ，启动xxl-job。
![](https://img-blog.csdnimg.cn/5e753a845fd44fb98e5af6271d2c5ed9.png)
再浏览器访问 http://localhost:9090/xxl-job-admin ，用户名admin 密码123456
登录成功，此时没有任务运行。定时任务管理器已经运行成功，我们开始集成到自己的项目。
![在这里插入图片描述](https://img-blog.csdnimg.cn/f11a740430a04a4da69b56349443770e.png)
三、将xxl-job集成到我们的springboot项目
3.1 新建Springboot 空项目

3.2 引入Maven依赖
![在这里插入图片描述](https://img-blog.csdnimg.cn/55ae9489782b4682b652e9c74f92d1f0.png)
```java
    <!-- SpringBoot集成Xxl-Job -->
    <dependency>
        <groupId>com.xuxueli</groupId>
        <artifactId>xxl-job-core</artifactId>
        <version>2.3.0</version>
    </dependency>
```
3.3.编写配置文件

在我们的springboot项目的 application-dev.yml 配置文件中新增xxl-job配置信息。注意：如果 accessToken 不设置的话，最好把它注释掉，或者 输入xxl-job 默认的 default_token ，否则调用的时候容易引起获取不到token的报错。
![在这里插入图片描述](https://img-blog.csdnimg.cn/da17db9b87c64bbc91ac50868a392871.png)
```java
# Xxl-Job分布式定时任务调度中心
xxl:
  job:
    admin:
      # 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。 
      addresses: http://localhost:9090/xxl-job-admin
      # addresses: http://192.168.110.2:9090/xxl-job-admin
    # 执行器通讯TOKEN [选填]：非空时启用 系统默认 default_token
    accessToken: default_token
    executor:
      # 执行器的应用名称
      appname: mls-xxl-job
      # 执行器注册 [选填]：优先使用该配置作为注册地址
      address: ""
      # 执行器IP [选填]：默认为空表示自动获取IP
      ip: ""
      # 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999
      port: 9999
      # 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；
      logpath: D:\Codes\logs
      #logpath: /data/logs/mls/job
      # 执行器日志文件保存天数 [选填] ： 过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能；
      logretentiondays: 7
```
3.3 编写配置类
将 XxlJobConfig 配置类放入项目中的配置类文件夹下，此类可以不用修改。注意：如果上一个配置import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

```java
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName： XxlJobConfig
 * @Description: xxl-job依赖配置
 * @author： 
 * @date： 2022年12月07日 08:37
 * @version： 1.0
 */
@Configuration  //是否开启xxl-job定时任务，注释掉 //@Configuration 则不开启定时任务
@Data
@Slf4j
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.address}")
    private String address;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobHelper.log(">>>>>>>>>>> xxl-job config init.>>>>>>>>>>>");
        System.out.println("=============== xxl-job config init.===============");

        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }
}
```

3.4 编写测试类

```java
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName： test 
 * @Description: XxlJobTest
 * @author： 
 * @date： 2022年12月07日 12:58
 * @version： 1.0
 */
@Slf4j
@Component
public class test {

    @XxlJob("xxlJobTest")
    public ReturnT<String> xxlJobTest(String date) {
//        log.info("---------xxlJobTest定时任务执行成功--------");
        System.out.println("---------xxlJobTest定时任务执行成功--------");
        return ReturnT.SUCCESS;
    }
}
```

3.5 启动项目
先启动调度中心（xxl-job-admin模块），再启动自己的springBoot服务（xxl-job-admin模块的端口已改为9090，所以本地在浏览器访问 http://localhost:9090/xxl-job-admin ，用户名admin 密码123456
![在这里插入图片描述](https://img-blog.csdnimg.cn/4975392cb1ca44f7b4a7c264077fc169.png)
3.6 任务调度中心，配置服务
3.6.1 执行器管理--新增执行器
![在这里插入图片描述](https://img-blog.csdnimg.cn/1393a1122ad740f79a4ec982ba98166d.png)
![!\[在这里插入图片描述\](https://img-blog.csdnimg.cn/98ad361eef674309ad00c3b31a9db04d.png](https://img-blog.csdnimg.cn/9b3d229f97bb4cb790e3e4e98896fdd6.png)

3.6.2 任务管理--新增任务
![在这里插入图片描述](https://img-blog.csdnimg.cn/cce2dff8cf81400b915ce520386dc3fc.png)

3.6.3 任务管理--执行任务
测试可以点击“执行一次”，如果想一直运行，点击下拉的“启动”即可。至此，springboot 简单集成 xxl-job 已完成。
![在这里插入图片描述](https://img-blog.csdnimg.cn/d661833e2fad47c28edd3d614fd0335f.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/030a2e906e404feb916c8c7b871296e0.png)

项目测试代码地址：[https://github.com/dlsgithub/Springboot-xxljob-Nacos](https://github.com/dlsgithub/Springboot-xxljob-Nacos)

参考文章：[https://www.cnblogs.com/csnjava/p/17060093.html](https://www.cnblogs.com/csnjava/p/17060093.html)

# 二、xxljob+nacos

【摘要】 最近需要部署一套xxl-job,本来直接打成jar包部署就行了,但是jar包方式每次修改相关的配置(比如端口号)后需要重新打包再启动,相当麻烦，所以就想借用nacos的配置中心功能来进行动态配置（直接在Nacos里面修改配置文件内容，再重启jar包即可，不需要重启打jar包）,这样比较简单.所以本文中主要使用了nacos的配置中心功能,注册中心虽然引入了,但是并没有使用太多,最多起到健康监控的功能,毕竟正常情况下没有其他的地方会调用xxl-job

1. nacos部署
   https://github.com/alibaba/nacos/releases 下载对应系统的nacos包即可

以windows为例

解压后进入bin目录启动,启动命令为:

```java
startup.cmd -m standalone
```

单机模式启动,启动成功后访问localhost:8848/nacos,默认用户名/密码: nacos/nacos
2. xxl-job-admin改造
   2.1 引入nacos依赖
   修改xxl-job-admin模块pom文件,增加如下依赖

```java
		<!-- nacos-discovery -->
		<dependency>
			<groupId>com.alibaba.cloud</groupId>
			<artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
			<version>2021.0.1.0</version>
		</dependency>

		<!-- nacos-config -->
		<dependency>
			<groupId>com.alibaba.cloud</groupId>
			<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
			<version>2021.0.1.0</version>
		</dependency>

		<!-- spring-cloud-bootstrap -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-bootstrap</artifactId>
			<version>3.0.1</version>
		</dependency>
```
2.2 nacos增加配置
![在这里插入图片描述](https://img-blog.csdnimg.cn/18490ae119af450ebf4c00f99fe04fc1.png)
选择对应的ns后新增配置
![在这里插入图片描述](https://img-blog.csdnimg.cn/448f173048104fefa7f95119428f9c1f.png)
data-id的默认规则为:
```java
${prefix}-${spring.profiles.active}.${file-extension}
```
prefix 默认为 spring.application.name 的值，也可以通过配置项 spring.cloud.nacos.config.prefix来配置。
spring.profiles.active 即为当前环境对应的 profile，详情可以参考 Spring Boot文档。 注意：当 spring.profiles.active 为空时，对应的连接符 - 也将不存在，dataId 的拼接格式变成 ${prefix}.${file-extension}
file-exetension 为配置内容的数据格式，可以通过配置项 spring.cloud.nacos.config.file-extension 来配置。目前只支持 properties 和 yaml 类型。
由于没有设置spring.profiles.active,所以我们设置的data-id是 xxl-job-admin.properties
![在这里插入图片描述](https://img-blog.csdnimg.cn/e70a235c717a4e6d8705c6b6d2a1a7e3.png)
配置格式选择 properties ，然后将application.properties中的配置信息都放到nacos的配置内容 中
```java
### web
server.port=9090
server.servlet.context-path=/xxl-job-admin

### actuator
management.server.servlet.context-path=/actuator
management.health.mail.enabled=false

### resources
spring.mvc.servlet.load-on-startup=0
spring.mvc.static-path-pattern=/static/**
spring.resources.static-locations=classpath:/static/

### freemarker
spring.freemarker.templateLoaderPath=classpath:/templates/
spring.freemarker.suffix=.ftl
spring.freemarker.charset=UTF-8
spring.freemarker.request-context-attribute=request
spring.freemarker.settings.number_format=0.##########

### mybatis
mybatis.mapper-locations=classpath:/mybatis-mapper/*Mapper.xml
#mybatis.type-aliases-package=com.xxl.job.admin.core.model

### xxl-job, datasource
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

### datasource-pool
spring.datasource.type=com.zaxxer.hikari.HikariDataSource
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.pool-name=HikariCP
spring.datasource.hikari.max-lifetime=900000
spring.datasource.hikari.connection-timeout=10000
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=1000

### xxl-job, email
spring.mail.host=smtp.qq.com
spring.mail.port=25
spring.mail.username=xxx@qq.com
spring.mail.from=xxx@qq.com
spring.mail.password=xxx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory

### xxl-job, access token
xxl.job.accessToken=default_token

### xxl-job, i18n (default is zh_CN, and you can choose "zh_CN", "zh_TC" and "en")
xxl.job.i18n=zh_CN

## xxl-job, triggerpool max size
xxl.job.triggerpool.fast.max=200
xxl.job.triggerpool.slow.max=100

### xxl-job, log retention days
xxl.job.logretentiondays=30

```
2.3 修改配置文件
依赖引入后,需要进行xxl-job-admin模块配置文件的修改,主要替换为nacos的配置
```java
spring.application.name=xxl-job-admin
### nacos
# nacos地址
nacos.server-addr=127.0.0.1:8848
# 分组
nacos.group=DEFAULT_GROUP
# 命名空间
nacos.namespace=public
# 服务发现配置
spring.cloud.nacos.discovery.server-addr=${nacos.server-addr}
spring.cloud.nacos.discovery.group=${nacos.group}
spring.cloud.nacos.discovery.namespace=${nacos.namespace}
spring.cloud.nacos.discovery.enabled=true
# 配置中心配置
spring.cloud.nacos.config.server-addr=${nacos.server-addr}
spring.cloud.nacos.config.group=${nacos.group}
spring.cloud.nacos.config.namespace=${nacos.namespace}
spring.cloud.nacos.config.enabled=true
spring.cloud.nacos.config.file-extension=properties
```
如果是本地单机启动的nacos,可以简单配置下,如果是正式环境的配置,还需要增加group,namespace等配置,具体参考nacos官网
此时xxl-job-admin项目程序里只留下nacos的相关配置信息。
2.4 测试
启动xxl-job-admin,发现无异常，此时xxl-job-admin服务的端口号为9090：
![在这里插入图片描述](https://img-blog.csdnimg.cn/c65eeab314ec430898a72e63fbbd1a5f.png)
修改nacos配置中心,端口号修改为为9092
![在这里插入图片描述](https://img-blog.csdnimg.cn/e8cbcc4572e4414684b18ddb37e2d4cd.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/cd05d12a559941659d7145489da117b2.png)
,重启xxl-job-admin,发现端口号确实变成了9092,成功.
![在这里插入图片描述](https://img-blog.csdnimg.cn/3bf2a2670bea41b588c8fe42162e83b2.png)
项目测试代码地址：[https://github.com/dlsgithub/Springboot-xxljob-Nacos](https://github.com/dlsgithub/Springboot-xxljob-Nacos)

参考文章：[https://bbs.huaweicloud.com/blogs/385751](https://bbs.huaweicloud.com/blogs/385751)

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
