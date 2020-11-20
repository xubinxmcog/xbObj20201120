package com.enuos.live.utils;

/**
 * @ClassName RandomUtil
 * @Description: TODO 产生ID值
 * @Author xubin
 * @Date 2020/4/8
 * @Version V1.0
 **/
public class RandomUtil {

    public static String getRandom() {
        return System.currentTimeMillis() + "" + cn.hutool.core.util.RandomUtil.randomInt(999);
    }

    /**
     * 六位随机数
     * @return
     */
    public static String getRandomSixNum() {
        int rannum = (int)(Math.random()*(999999-100000+1))+100000;
        return ""+rannum;
    }
}
