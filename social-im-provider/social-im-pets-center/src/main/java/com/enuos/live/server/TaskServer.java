package com.enuos.live.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @ClassName TaskServer
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/10
 * @Version V2.0
 **/
@Slf4j
@Component
public class TaskServer implements CommandLineRunner {

    /**
     * 设置每60秒执行一次
     */
    public static final long TIME_SECONDS = 60;

    public static long midTime = TIME_SECONDS;

    /**
     * @MethodName: time
     * @Description: TODO 定时扣减饱食度/心情 获得金币任务
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 17:58 2020/10/10
     **/
    public static void time() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                midTime--;
//                long hh = midTime / 60 / 60 % 60;
//                long mm = midTime / 60 % 60;
//                long ss = midTime % 60;
//                System.out.println("还剩" + hh + "小时" + mm + "分钟" + ss + "秒");
                if (midTime == 0) {
                    try {
                        System.out.println("执行任务....");
                    } catch (Exception e) {
                        log.info("定时扣减饱食度/心情,获得金币任务异常,TaskServer.time");
                        e.printStackTrace();
                    }
                    midTime = TIME_SECONDS;
                }
            }
        }, 0, 1000);

    }

    /**
     * 方法启动类
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        time();
    }
}
