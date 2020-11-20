package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description 邀请用户
 * @Author wangyingjie
 * @Date 2020/8/7
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InviteUser extends Base implements Serializable {

    /** 主键 */
    private Integer id;

    /** 邀请平台[1：微信；2：QQ；3：朋友圈；4：QQ空间；5：微博；6：面对面扫码] */
    private Integer platform;

    /** 被邀请用户账号[目前只手机号] */
    private String toUserAccount;

    /** 手机验证码 */
    private String code;

    /** 被邀请用户ID */
    private Long toUserId;

    /** 首次登陆时间 */
    private LocalDateTime loginTime;

}
