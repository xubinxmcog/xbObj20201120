package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description 会员中心
 * @Author wangyingjie
 * @Date 2020/6/30
 * @Modified
 */
@Data
public class Member extends Base implements Serializable {

    private static final long serialVersionUID = 7568790435369628933L;

    /** 是否会员[-1：过期会员；0：否； 1：是] */
    private Integer isMember;

    /** 会员说明URL */
    private String shipUrl;

    /** 昵称 */
    private String nickName;

    /** 头像 */
    private String thumbIconUrl;

    /** vip等级 */
    private Integer vip;

    /** vip等级 */
    private Integer vipLine;

    /** vip标识 */
    private String vipIconUrl;

    /** 成长值 */
    private Integer growth;

    /** 成长值 */
    private Integer growthLine;

    /** 期限 */
    private String expiration;

    /** 到期时间 */
    private LocalDateTime expirationTime;

    /** 权益列表 */
    private List<MemberInterest> interestList;

}