package com.enuos.live.job;

import com.enuos.live.mapper.GashaponMapper;
import com.enuos.live.pojo.GashaponUser;
import com.enuos.live.service.GashaponService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/23
 * @Modified
 */
@Data
@Slf4j
@Component
@Configuration
@EnableScheduling
public class GashaponJob implements SchedulingConfigurer {

    @Autowired
    private GashaponService gashaponService;

    @Autowired
    private GashaponMapper gashaponMapper;

    /**
     * 扭蛋定时开奖
     * Callback allowing a {@link TaskScheduler
     * TaskScheduler} and specific {@link Task Task}
     * instances to be registered against the given the {@link ScheduledTaskRegistrar}.
     *
     * @param taskRegistrar the registrar to be configured.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        List<Map<String, Object>> cronList = gashaponMapper.getGashaponCrontab();
        cronList.forEach(cronMap -> taskRegistrar.addTriggerTask(
                doJob(MapUtils.getString(cronMap, "code")),
                triggerContext -> new CronTrigger(MapUtils.getString(cronMap, "cron")).nextExecutionTime(triggerContext)));
    }
    
    /** 
     * @Description: do something
     * @Param: [code] 
     * @Return: java.lang.Runnable 
     * @Author: wangyingjie
     * @Date: 2020/6/23 
     */ 
    private Runnable doJob(String code) {
        return () -> {
            int time = LocalTime.now().toSecondOfDay();

            log.info("==========[Gashapon begin code is [{}]]==========", code);

            String suffix = gashaponMapper.getSuffix(code, time);
            if (StringUtils.isBlank(suffix)) {
                log.error("Lucky draw begins time is [{}] suffix is [{}]", time, suffix);
                return;
            }

            String finalCode = getPrefix().concat(code).concat("_").concat(suffix);

            // 获取所有参与用户
            List<GashaponUser> list = gashaponMapper.getJoinUser(finalCode);
            if (CollectionUtils.isEmpty(list)) {
                log.info("==========[Gashapon end no user code is [{}]]==========", finalCode);
                return;
            }

            gashaponService.luckyUser(code, list);

            log.info("==========[Gashapon end]==========");
        };
    }

    /**
     * @Description: prefix
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/6/23
     */
    private String getPrefix() {
        return LocalDate.now().toString().replace("-", "").concat("_");
    }

}
