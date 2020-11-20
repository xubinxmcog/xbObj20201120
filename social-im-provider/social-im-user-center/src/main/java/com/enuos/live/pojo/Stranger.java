package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 陌生人
 * @Author wangyingjie
 * @Date 2020/7/3
 * @Modified
 */
@Data
public class Stranger extends Base implements Serializable {

    private static final long serialVersionUID = -2570924643194420391L;

    /** 头像 */
    private String iconUrl;

    /** 缩略图 */
    private String thumbIconUrl;

    /** 头像框 */
    private String iconFrame;

    /** 昵称 */
    private String nickName;

    /** 签名 */
    private String signature;

    /** 性别[1 男 2 女] */
    private Integer sex;

    /** 地区 */
    private String area;

    /** 是否注销[0 否 1 是] */
    private Integer isDel;

    /** 是否好友[0 否 1 是] */
    private Integer isFriend;

    /** 备注 */
    private String remark;

    /** 是否会员[-1：过期会员；0：否； 1：是] */
    private Integer isMember;

    /** vip */
    private Integer vip;

    /** vip标识 */
    private String vipIconUrl;

    /** 等级 */
    private Integer level;

    /** 魅力值 */
    private Long charm;

    /** 是否拉黑[0 否 1 是] */
    private Integer isPullBlack;

    /** 在线状态[0：离线；1：在线；2：游戏中；3：直播中；4：免打扰] */
    private Integer onLineStatus;

    /** 背景 */
    private List<Background> backgroundList;

}
