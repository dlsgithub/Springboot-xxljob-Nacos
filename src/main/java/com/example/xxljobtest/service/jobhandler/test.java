package com.example.xxljobtest.service.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName： XxlJobTest
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
