package com.enuos.live.manager;

/**
 * @Description 活动中心枚举
 * @Author wangyingjie
 * @Date 2020/7/10
 * @Modified
 */
public enum ActivityEnum {

    ACT0001("ACT0001","丹枫迎秋", "秋日活动。"),

    ACT000101("ACT0001.01_1","每日动态分享", "每日动态分享。"),
    ACT000102("ACT0001.02_1","玩一次对战游戏", "玩一次对战游戏。"),
    ACT000103("ACT0001.03_1","玩一次互动游戏", "玩一次互动游戏。"),
    ACT000104("ACT0001.04_1","玩语音房20分钟", "玩语音房20分钟。"),
    ACT000105("ACT0001.05_1","语音房送出任意礼物", "语音房送出任意礼物。"),
    ACT000106("ACT0001.06_1","会员专属", "会员专属。"),
    ACT000107("ACT0001.07_1","2000金币兑换", "2000金币兑换。"),
    ACT000108("ACT0001.08_1","15钻石兑换", "15钻石兑换。"),

    ACT0002("ACT0002","国庆", "国庆活动。"),

    ACT0005("ACT0005","金秋送福", "金秋送福活动。");

    private String code;

    private String title;

    private String description;

    ActivityEnum(String code, String title, String description) {
        this.code = code;
        this.title = title;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

}