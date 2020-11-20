package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Description 微信平台绑定支付信息
 * @Author wangyingjie
 * @Date 2020/11/6
 * @Modified
 */
@Data
public class WeChat {

    /** 用户Id */
    @NotNull(message = "用户Id不能为空")
    private Long userId;

    /** 唯一标识 */
    @NotBlank(message = "unionId不能为空")
    private String unionId;

    /** 支付标识 */
    @NotBlank(message = "openId不能为空")
    private String openId;

    /** 昵称 */
    @NotBlank(message = "微信昵称不能为空")
    private String weChatNickName;

}
