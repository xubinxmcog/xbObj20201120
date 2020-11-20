package com.enuos.live.manager;

/**
 * @Description 令状任务
 * @Author wangyingjie
 * @Date 2020/10/12
 * @Modified
 */
public enum WritTaskEnum {

    WRIT0001("WRIT0001","每日登录"),
    WRIT0002("WRIT0002","发布动态"),
    WRIT0003("WRIT0003","好友动态点赞"),
    WRIT0004("WRIT0004","与好友玩一局对战游戏"),
    WRIT0005("WRIT0005","进行一局互动游戏"),
    WRIT0006("WRIT0006","语音房观看10分钟"),
    WRIT0007("WRIT0007","语音房上麦1次");

    private String code;

    private String title;

    WritTaskEnum(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

}
