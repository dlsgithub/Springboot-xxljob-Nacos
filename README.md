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