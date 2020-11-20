package com.enuos.live.constant;

import com.enuos.live.result.BaseCodeMsg;

/**
 * @Description 成功响应码
 * @Author wangyingjie
 * @Date 16:43 2020/4/1
 * @Modified
 */
public class SuccessCode {

    public static final int SUCCESS_CODE = 1;

    /** 登陆跳转资料补全页面 */
    public static final BaseCodeMsg LOGIN_TO_COMPLETE_USERINFO = BaseCodeMsg.app(1, "您还需要一个昵称...");

    /** 登陆跳转联系客服页面 */
    public static final BaseCodeMsg LOGIN_TO_ACCOUNT_SUSPENDED = BaseCodeMsg.app(2, "您的账号已被查封...请联系客服...");

}
