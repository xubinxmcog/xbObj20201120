package com.enuos.live.manager;

/**
 * @Description 扭蛋抽奖
 * @Author wangyingjie
 * @Date 2020/7/27
 * @Modified
 */
public enum GashaponEnum {
    /** 参与人数造假 */
    LG0001("LG0001",1, 10),
    LG0002("LG0002",5, 20),
    LG0003("LG0003",5, 10),
    LG0004("LG0004",5, 10),
    LG0005("LG0005",5, 15);

    private String code;

    private Integer minJoinNum;

    private Integer maxJoinNum;

    GashaponEnum(String code, Integer minJoinNum, Integer maxJoinNum) {
        this.code = code;
        this.minJoinNum = minJoinNum;
        this.maxJoinNum = maxJoinNum;
    }

    public String getCode() {
        return code;
    }

    public Integer getMinJoinNum() {
        return minJoinNum;
    }

    public Integer getMaxJoinNum() {
        return maxJoinNum;
    }
}
