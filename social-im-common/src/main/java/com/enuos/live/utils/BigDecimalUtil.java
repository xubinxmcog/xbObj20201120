package com.enuos.live.utils;

import java.math.BigDecimal;

/**
 * @Description BigDecimal工具类
 * @Author wangyingjie
 * @Date 2020/5/19
 * @Modified
 */
public class BigDecimalUtil {

    /**
     * 重写四则运算加法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Integer nAdd(Integer n1, Integer n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.add(b2).intValue();
    }

    /**
     * 重写四则运算加法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Long nAdd(Long n1, Long n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.add(b2).longValue();
    }

    /**
     * 重写四则运算加法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static String nAdd(String n1, String n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.add(b2).toString();
    }

    /**
     * 重写四则运算加法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Double nAdd(Double n1, Double n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.add(b2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 重写四则运算加法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static BigDecimal nAdd(BigDecimal n1, BigDecimal n2) {
        return handleData(n1).add(handleData(n2));
    }

    /**
     * 重写四则运算减法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Integer nSub(Integer n1, Integer n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.subtract(b2).intValue();
    }

    /**
     * 重写四则运算减法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Long nSub(Long n1, Long n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.subtract(b2).longValue();
    }

    /**
     * 重写四则运算减法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static String nSub(String n1, String n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.subtract(b2).toString();
    }

    /**
     * 重写四则运算减法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Double nSub(Double n1, Double n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.subtract(b2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 重写四则运算减法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static BigDecimal nSub(BigDecimal n1, BigDecimal n2) {
        return handleData(n1).subtract(handleData(n2));
    }

    /**
     * 重写四则运算乘法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static String nMul(String n1, String n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.multiply(b2).toString();
    }

    /**
     * 重写四则运算乘法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Double nMul(Double n1, Double n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        return b1.multiply(b2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 重写四则运算乘法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static BigDecimal nMul(BigDecimal n1, BigDecimal n2) {
        return handleData(n1).multiply(handleData(n2));
    }

    /**
     * 重写四则运算除法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static String nDiv(String n1, String n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        if (b2.compareTo(BigDecimal.ZERO) == 0) return "0";
        return b1.divide(b2, 10, BigDecimal.ROUND_HALF_DOWN).toString();
    }

    /**
     * 重写四则运算除法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static Double nDiv(Double n1, Double n2) {
        BigDecimal b1 = new BigDecimal(handleData(n1));
        BigDecimal b2 = new BigDecimal(handleData(n2));
        if (b2.compareTo(BigDecimal.ZERO) == 0) return 0d;
        return b1.divide(b2, 10, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    /**
     * 重写四则运算除法
     *
     * @param n1
     * @param n2
     * @return
     */
    public static BigDecimal nDiv(BigDecimal n1, BigDecimal n2) {
        if (handleData(n2).compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return handleData(n1).divide(handleData(n2), 10, BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * 处理数据
     *
     * @param i
     * @return
     */
    private static Integer handleData(Integer i) {
        return i == null ? 0 : i;
    }

    /**
     * 处理数据
     *
     * @param l
     * @return
     */
    private static Long handleData(Long l) {
        return l == null ? 0L : l;
    }

    /**
     * 处理数据
     *
     * @param s
     * @return
     */
    private static String handleData(String s) {
        return s == null || "".equals(s) ? "0" : s;
    }

    /**
     * 处理数据
     *
     * @param d
     * @return
     */
    private static Double handleData(Double d) {
        return d == null ? 0d : d;
    }

    /**
     * 处理数据
     *
     * @param b
     * @return
     */
    private static BigDecimal handleData(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }
}