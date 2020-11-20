package com.enuos.live.pay.aliPay.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName WXPayConstants
 * @Description: TODO
 * @Author xubin
 * @Date 2020/8/3
 * @Version V2.0
 **/
@Component
public class AliPayConstants {

    /**
     * 应用号
     */
    public static String APP_ID;
    @Value("${aliyun.pay.app_id}")
    public void setAPP_ID(String APP_ID) {
        this.APP_ID = APP_ID;
    }
    /**
     * APP应用号
     */
    public static String APPP_ID;
    @Value("${aliyun.pay.appp_id}")
    public void setAPPP_ID(String APPP_ID) {
        this.APPP_ID = APPP_ID;
    }

    /**
     * 商户的私钥
     */
    public static String APP_PRIVATE_KEY;
    @Value("${aliyun.pay.app_private_key}")
    public void setAPP_PRIVATE_KEY(String APP_PRIVATE_KEY) {
        this.APP_PRIVATE_KEY = APP_PRIVATE_KEY;
    }

    /**
     * 字符集
     */
    public static String CHARSET;
    @Value("${aliyun.pay.charset}")
    public void setCHARSET(String CHARSET) {
        this.CHARSET = CHARSET;
    }

    /**
     * 支付宝公钥
     */
    public static String ALIPAY_PUBLIC_KEY;
    @Value("${aliyun.pay.alipay_public_key}")
    public void setALIPAY_PUBLIC_KEY(String ALIPAY_PUBLIC_KEY) {
        this.ALIPAY_PUBLIC_KEY = ALIPAY_PUBLIC_KEY;
    }

    /**
     * 支付宝网关地址
     */
    public static String GATEWAY;
    @Value("${aliyun.pay.gateway}")
    public void setGATEWAY(String GATEWAY) {
        this.GATEWAY = GATEWAY;
    }
    /**
     * 成功付款回调
     */
    public static String PAY_NOTIFY;
    @Value("${aliyun.pay.pay_notify}")
    public void setPAY_NOTIFY(String PAY_NOTIFY) {
        this.PAY_NOTIFY = PAY_NOTIFY;
    }
    /**
     * 支付成功回调
     */
    public static String REFUND_NOTIFY;
    @Value("${aliyun.pay.refund_notify}")
    public void setREFUND_NOTIFY(String REFUND_NOTIFY) {
        this.REFUND_NOTIFY = REFUND_NOTIFY;
    }
    /**
     * 前台通知地址
     */
    public static String RETURN_URL;
    @Value("${aliyun.pay.return_url}")
    public void setRETURN_URL(String RETURN_URL) {
        this.RETURN_URL = RETURN_URL;
    }
    /**
     * 参数类型
     */
    public static String PARAM_TYPE;
    @Value("${aliyun.pay.param_type}")
    public void setPARAM_TYPE(String PARAM_TYPE) {
        this.PARAM_TYPE = PARAM_TYPE;
    }
    /**
     * 签名方式
     */
    public static String SIGN_TYPE;
    @Value("${aliyun.pay.sign_type}")
    public void setSIGN_TYPE(String SIGN_TYPE) {
        this.SIGN_TYPE = SIGN_TYPE;
    }

    /**
     * 成功标识
     */
    public static String SUCCESS_REQUEST = "TRADE_SUCCESS";
    @Value("${aliyun.pay.success_request}")
    public void setSUCCESS_REQUEST(String SUCCESS_REQUEST) {
        this.SUCCESS_REQUEST = SUCCESS_REQUEST;
    }

    /**
     * 交易关闭回调(当该笔订单全部退款完毕,则交易关闭)
     */
    public static String TRADE_CLOSED;
    @Value("${aliyun.pay.trade_closed}")
    public void setTRADE_CLOSED(String TRADE_CLOSED) {
        this.TRADE_CLOSED = TRADE_CLOSED;
    }

    /**
     * 收款方账号
     */
    public static String SELLER_ID;
    @Value("${aliyun.pay.seller_id}")
    public void setSELLER_ID(String SELLER_ID) {
        this.SELLER_ID = SELLER_ID;
    }

    /**
     * 支付成功的通知地址
     */
    public static String NOTIFY_URL;
    @Value("${aliyun.pay.notify_url}")
    public void setNOTIFY_URL(String NOTIFY_URL) {
        this.NOTIFY_URL = NOTIFY_URL;
    }

    /**
     * 应用公钥证书路径
     */
    public static String APP_CERT_PATH;
    @Value("${aliyun.pay.app_cert_path}")
    public void setAPP_CERT_PATH(String APP_CERT_PATH) {
        this.APP_CERT_PATH = APP_CERT_PATH;
    }

    /**
     * 应用APP公钥证书路径
     */
    public static String APPP_CERT_PATH;
    @Value("${aliyun.pay.appp_cert_path}")
    public void setAPPP_CERT_PATH(String APPP_CERT_PATH) {
        this.APPP_CERT_PATH = APPP_CERT_PATH;
    }

    /**
     * 支付宝公钥证书文件路径
     */
    public static String CERT_PATH;
    @Value("${aliyun.pay.cert_path}")
    public void setCERT_PATH(String CERT_PATH) {
        this.CERT_PATH = CERT_PATH;
    }

    /**
     * 支付宝APP公钥证书文件路径
     */
    public static String CERTP_PATH;
    @Value("${aliyun.pay.certp_path}")
    public void setCERTP_PATH(String CERTP_PATH) {
        this.CERTP_PATH = CERTP_PATH;
    }

    /**
     * 支付宝CA根证书文件路径
     */
    public static String ROOT_CERT_PATH;
    @Value("${aliyun.pay.root_cert_path}")
    public void setROOT_CERT_PATH(String ROOT_CERT_PATH) {
        this.ROOT_CERT_PATH = ROOT_CERT_PATH;
    }

    /**
     * 支付宝CA根证书文件路径
     */
    public static String ROOTP_CERT_PATH;
    @Value("${aliyun.pay.rootp_cert_path}")
    public void setROOTP_CERT_PATH(String ROOTP_CERT_PATH) {
        this.ROOTP_CERT_PATH = ROOTP_CERT_PATH;
    }
}
