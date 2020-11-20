package com.enuos.live.job;

import com.enuos.live.mapper.TaskCenterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @Description 任务中心定时任务
 * @Author wangyingjie
 * @Date 2020/6/23
 * @Modified
 */
@Slf4j
@Component
public class TaskCenterJob {

    @Autowired
    private TaskCenterMapper taskCenterMapper;

    /**
     * @Description: 任务中心 每周一早上5点清空数据
     * 1 清空上周活跃及领奖记录
     * 2 清空上周每日任务追踪及领奖记录
     * @Param: []
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @Scheduled(cron = "0 0 5 ? * MON")
    @Transactional
    public void taskCenter() {
        log.info("==========[TaskCenterJob begin]");
        long beginTime = System.currentTimeMillis();

        LocalDate current = LocalDate.now();
        taskCenterMapper.deleteTaskActive(current);
        taskCenterMapper.deleteTaskFollow(current);
        taskCenterMapper.deleteTaskRewardRecord(current);

        long endTime = System.currentTimeMillis();
        log.info("==========[TaskCenterJob end take time {}]", endTime - beginTime);
    }

}
