package com.enuos.live.pay.aliPay.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayOpenOperationOpenbizmockBizQueryModel;
import com.alipay.api.request.AlipayOpenOperationOpenbizmockBizQueryRequest;
import com.enuos.live.pay.aliPay.constants.AliPayConstants;

//import com.alipay.easysdk.kernel.Config;

/**
 * @ClassName AlipayConfig
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/27
 * @Version V1.0
 **/
public class AliAppPayConfig {

    /**
     * 支付宝请求客户端入口
     */
    private volatile static AlipayClient alipayClient = null;
    /**
     * 支付宝请求客户端入口
     */
    private volatile static AlipayClient alipayClientCertificate = null;

    /**
     * 构造client
     */
    private volatile static CertAlipayRequest certAlipayRequest = null;

    /**
     * 不可实例化
     */
    private AliAppPayConfig() {
    }

    /**
     * 双重锁单例
     * 普通公钥方式
     *
     * @return 支付宝请求客户端实例
     */
    public static AlipayClient getInstance() {
        if (alipayClient == null) {
            synchronized (AliAppPayConfig.class) {
                if (alipayClient == null) {
                    alipayClient = new DefaultAlipayClient(AliPayConstants.GATEWAY, AliPayConstants.APP_ID,
                            AliPayConstants.APP_PRIVATE_KEY, AliPayConstants.PARAM_TYPE, AliPayConstants.CHARSET,
                            AliPayConstants.ALIPAY_PUBLIC_KEY, AliPayConstants.SIGN_TYPE);
                }
            }
        }
        return alipayClient;
    }

    /**
     * 双重锁单例
     * 证书方式
     * APP 使用
     *
     * @return 支付宝请求客户端实例
     */
    public static AlipayClient getAppCertificateInstance() {
        if (alipayClientCertificate == null) {
            synchronized (AliAppPayConfig.class) {
                if (alipayClientCertificate == null) {
                    try {
                        alipayClientCertificate = new DefaultAlipayClient(getCertAlipayRequestInstance());
                    } catch (AlipayApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return alipayClientCertificate;
    }

    private static CertAlipayRequest getCertAlipayRequestInstance() {
        if (certAlipayRequest == null) {
            synchronized (AliAppPayConfig.class) {
                if (certAlipayRequest == null) {
                    certAlipayRequest = new CertAlipayRequest();
                    //设置网关地址
                    certAlipayRequest.setServerUrl(AliPayConstants.GATEWAY);
                    //设置应用Id
                    certAlipayRequest.setAppId(AliPayConstants.APPP_ID);
                    //设置应用私钥
                    certAlipayRequest.setPrivateKey(AliPayConstants.APP_PRIVATE_KEY);
                    //设置请求格式，固定值json
                    certAlipayRequest.setFormat(AliPayConstants.PARAM_TYPE);
                    //设置字符集
                    certAlipayRequest.setCharset(AliPayConstants.CHARSET);
                    //设置签名类型
                    certAlipayRequest.setSignType(AliPayConstants.SIGN_TYPE);
                    //设置应用公钥证书路径
                    certAlipayRequest.setCertPath(AliPayConstants.APPP_CERT_PATH);
                    //设置支付宝公钥证书路径
                    certAlipayRequest.setAlipayPublicCertPath(AliPayConstants.CERTP_PATH);
                    //设置支付宝根证书路径
                    certAlipayRequest.setRootCertPath(AliPayConstants.ROOTP_CERT_PATH);
                }
            }
        }
        return certAlipayRequest;
    }

    /**
     * @return 支付宝请求客户端实例
     */
    public static AlipayOpenOperationOpenbizmockBizQueryRequest getRequest() {
        // 初始化Request，并填充Model属性。实际调用时请替换为您想要使用的API对应的Request对象。
        AlipayOpenOperationOpenbizmockBizQueryRequest request = new AlipayOpenOperationOpenbizmockBizQueryRequest();
        AlipayOpenOperationOpenbizmockBizQueryModel model = new AlipayOpenOperationOpenbizmockBizQueryModel();
        model.setBizNo("test");
        request.setBizModel(model);
        return request;
    }
}
