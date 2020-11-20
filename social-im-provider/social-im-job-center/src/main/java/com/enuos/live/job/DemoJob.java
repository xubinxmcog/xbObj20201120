package com.enuos.live.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName DemoJob
 * @Description: TODO
 * @Author xubin
 * @Date 2020/11/18
 * @Version V2.0
 **/
@Slf4j
@Component
public class DemoJob {

    @XxlJob("demoTestJobHandler")
    public ReturnT<String> test(String param) throws Exception {
        log.info("执行DemoJob.test");
        return ReturnT.SUCCESS;
    }

}
