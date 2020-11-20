package com.enuos.test;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/8/7
 * @Modified
 */
public class TestLocalDate {

    public static void main(String[] args) {

    }

    public static void timeTask1() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // 在不在房间



                System.out.println("你这小脑袋");
            }
        };

        Timer timer = new Timer();
        long delay = 5000L;

        timer.schedule(task, delay);
    }
}