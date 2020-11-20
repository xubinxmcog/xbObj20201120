package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 聊天列表返回
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
@Data
public class RoomVO extends Base implements Serializable {

    private static final long serialVersionUID = -2263306869158730962L;

    /**
     * 房间号
     */
    private Long roomId;

    /**
     * 封面
     */
    private String coverUrl;

    /**
     * 背景
     */
    private String backgroundUrl;

    /**
     * 房间名
     */
    private String name;

    /**
     * 是否加密
     */
    private Integer isLock;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 房主头像
     */
    private String thumbIconUrl;

    /**
     * 房间人数
     */
    private Long onNum;

    /**
     * 主题ID
     */
    private Integer themeId;

    /**
     * 主题名称
     */
    private String themeName;

    /**
     * 专辑ID
     */
    private Integer albumId;

    /**
     * 专辑标题
     */
    private String albumTitle;

    /**
     * 专辑封面图标
     */
    private String coverUrlSmall;

}
