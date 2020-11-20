package com.enuos.live.pay.weChatPay.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName WeChatPayConstants
 * @Description: TODO
 * @Author xubin
 * @Date 2020/8/5
 * @Version V2.0
 **/
@Component
public class WeChatPayConstants {

    /**
     * 服务号appId
     */
    public static String APP_ID;
    @Value("${wx.app_id}")
    public void setAPP_ID(String APP_ID) {
        this.APP_ID = APP_ID;
    }

    /**
     * App应用 appId
     */
    public static String AAPP_ID;
    @Value("${wx.aapp_id}")
    public void setAAPP_ID(String AAPP_ID) {
        this.AAPP_ID = AAPP_ID;
    }

    /**
     * 服务号AppSecret
     */
    public static String APP_SECRET = "";
    @Value("${wx.app_secret}")
    public void setAPP_SECRET(String APP_SECRET) {
        this.APP_SECRET = APP_SECRET;
    }

    /**
     * 支付商户号
     */
    public static String MCH_ID;
    @Value("${wx.mch_id}")
    public void setMCH_ID(String MCH_ID) {
        this.MCH_ID = MCH_ID;
    }

    /**
     * API密钥
     */
    public static String API_KEY;
    @Value("${wx.api_key}")
    public void setAPI_KEY(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    /**
     * 微信统一下单URL
     */
    public static String WECHAT_PAY_URL;
    @Value("${wx.wechat_pay_url}")
    public void setWECHAT_PAY_URL(String WECHAT_PAY_URL) {
        this.WECHAT_PAY_URL = WECHAT_PAY_URL;
    }

    /**
     * 统一下单-查询订单
     */
    public static String PAY_ORDERQUERY;
    @Value("${wx.pay_orderquery}")
    public void setPAY_ORDERQUERY(String PAY_ORDERQUERY) {
        this.PAY_ORDERQUERY = PAY_ORDERQUERY;
    }

    /**
     * 微信服务器调用支付结果通知路径
     */
    public static String WECHAT_PAY_NOTIFY;
    @Value("${wx.wechat_pay_notify}")
    public void setWECHAT_PAY_NOTIFY(String WECHAT_PAY_NOTIFY) {
        this.WECHAT_PAY_NOTIFY = WECHAT_PAY_NOTIFY;
    }

    /**
     * H5支付成功回至指定页面
     */
    public static String REDIRECT_URL;
    @Value("${wx.redirect_url}")
    public void setREDIRECT_URL(String REDIRECT_URL) {
        this.REDIRECT_URL = REDIRECT_URL;
    }

    /**
     * 企业付款请求Url
     */
    public static String BUSINESS_PAY_URL;
    @Value("${wx.business_pay_url}")
    public void setBUSINESS_PAY_URLL(String BUSINESS_PAY_URL) {
        this.BUSINESS_PAY_URL = BUSINESS_PAY_URL;
    }

    /**
     * 企业付款请求Url
     */
    public static String BUSINESS_SELECT_URL;
    @Value("${wx.business_select_url}")
    public void setBUSINESS_SELECT_URL(String BUSINESS_SELECT_URL) {
        this.BUSINESS_SELECT_URL = BUSINESS_SELECT_URL;
    }

    /**
     * 签名类型 MD5
     */
    public static String SING_MD5;
    @Value("${wx.sing_md5}")
    public void setSING_MD5(String SING_MD5) {
        this.SING_MD5 = SING_MD5;
    }

    /**
     * 签名类型 HMAC-SHA256
     */
    public static String SING_HMACSHA256;
    @Value("${wx.sing_hmacsha256}")
    public void setSING_HMACSHA256(String SING_HMACSHA256) {
        this.SING_HMACSHA256 = SING_HMACSHA256;
    }

    /**
     * @Description: TODO 订单支付状态
     * @Author: xubin
     * @Date: 13:04 2020/8/10
     **/
    public static Map<String, String> RETURN_CODE_MAP = new HashMap();

    static {
        RETURN_CODE_MAP.put("SUCCESS", "支付成功");
        RETURN_CODE_MAP.put("REFUND", "转入退款");
        RETURN_CODE_MAP.put("NOTPAY", "未支付");
        RETURN_CODE_MAP.put("CLOSED", "已关闭");
        RETURN_CODE_MAP.put("REVOKED", "已撤销");
        RETURN_CODE_MAP.put("USERPAYING", "用户支付中");
        RETURN_CODE_MAP.put("PAYERROR", "支付失败(其他原因，如银行返回失败)");
    }
}
