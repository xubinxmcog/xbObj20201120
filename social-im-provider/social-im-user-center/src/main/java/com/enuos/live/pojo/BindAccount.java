package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 账户绑定
 * @Author wangyingjie
 * @Date 2020/9/10
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BindAccount extends Base implements Serializable {

    private static final long serialVersionUID = 6308036002470310418L;

    /** 手机号 */
    private String phone;

    /** 短信验证码 **/
    private String code;

    /** 微信唯一标识 */
    private String unionId;

    /** 微信或者QQ[openid] */
    private String openId;

    /** AppleUserId */
    private String appleUserId;

    /** 绑定类别[0：手机号绑定；1：微信绑定；2：QQ绑定] */
    private Integer type;

    /** 是否绑定手机[0：否；1：是] */
    private Integer isBindPhone;

    /** 是否绑定微信[0：否；1：是] */
    private Integer isBindWeChat;

    /** 是否绑定QQ[0：否；1：是] */
    private Integer isBindQQ;

    /** 是否认证[0：否；1：是] */
    private Integer isAuthentication;

}
