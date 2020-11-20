package com.enuos.live.manager;

/**
 * @Description 新人邀请任务
 * @Author wangyingjie
 * @Date 2020/8/6
 * @Modified
 */
public enum InviteEnum {

    IFT0001("IFT0001","邀请好友奖励1", "邀请首位好友赢额外奖励。"),
    IFT0002("IFT0002","邀请好友奖励2", "{0}玩小游戏达到10分钟。");

    private String code;

    private String title;

    private String description;

    InviteEnum(String code, String title, String description) {
        this.code = code;
        this.title = title;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

}
