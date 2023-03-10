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