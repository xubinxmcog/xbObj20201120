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
public class AlipayConfig {

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
    private AlipayConfig() {
    }

    /**
     * 双重锁单例
     * 普通公钥方式
     *
     * @return 支付宝请求客户端实例
     */
    public static AlipayClient getInstance() {
        if (alipayClient == null) {
            synchronized (AlipayConfig.class) {
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
     *
     * @return 支付宝请求客户端实例
     */
    public static AlipayClient getCertificateInstance() {
        if (alipayClientCertificate == null) {
            synchronized (AlipayConfig.class) {
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
            synchronized (AlipayConfig.class) {
                if (certAlipayRequest == null) {
                    certAlipayRequest = new CertAlipayRequest();
                    //设置网关地址
                    certAlipayRequest.setServerUrl(AliPayConstants.GATEWAY);
                    //设置应用Id
                    certAlipayRequest.setAppId(AliPayConstants.APP_ID);
                    //设置应用私钥
                    certAlipayRequest.setPrivateKey(AliPayConstants.APP_PRIVATE_KEY);
                    //设置请求格式，固定值json
                    certAlipayRequest.setFormat(AliPayConstants.PARAM_TYPE);
                    //设置字符集
                    certAlipayRequest.setCharset(AliPayConstants.CHARSET);
                    //设置签名类型
                    certAlipayRequest.setSignType(AliPayConstants.SIGN_TYPE);
                    //设置应用公钥证书路径
                    certAlipayRequest.setCertPath(AliPayConstants.APP_CERT_PATH);
                    //设置支付宝公钥证书路径
                    certAlipayRequest.setAlipayPublicCertPath(AliPayConstants.CERT_PATH);
                    //设置支付宝根证书路径
                    certAlipayRequest.setRootCertPath(AliPayConstants.ROOT_CERT_PATH);
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

//    /**
//     * 阿里新版支付服务调用 Easy
//     */
//    public static Config getOptions() {
//        Config config = new Config();
//        config.protocol = "https";
//        config.gatewayHost = "openapi.alipay.com";
//        config.signType = AliPayConstants.SIGN_TYPE;
//
//        config.appId = AliPayConstants.APP_ID;
//
//        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
//        config.merchantPrivateKey = AliPayConstants.APP_PRIVATE_KEY;
//
//        //注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
//        config.merchantCertPath = AliPayConstants.APP_CERT_PATH;//"<-- 请填写您的应用公钥证书文件路径，例如：/foo/appCertPublicKey_2019051064521003.crt -->";
//        config.alipayCertPath = AliPayConstants.CERT_PATH;//"<-- 请填写您的支付宝公钥证书文件路径，例如：/foo/alipayCertPublicKey_RSA2.crt -->";
//        config.alipayRootCertPath = AliPayConstants.ROOT_CERT_PATH;// "<-- 请填写您的支付宝根证书文件路径，例如：/foo/alipayRootCert.crt -->";
//
//        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
//        // config.alipayPublicKey = "<-- 请填写您的支付宝公钥，例如：MIIBIjANBg... -->";
//
//        //可设置异步通知接收服务地址（可选）
//        config.notifyUrl = AliPayConstants.NOTIFY_URL;// "<-- 请填写您的支付类接口异步通知接收服务地址，例如：https://www.test.com/callback -->";
//
//        //可设置AES密钥，调用AES加解密相关接口时需要（可选）
////        config.encryptKey = "<-- 请填写您的AES密钥，例如：aa4BtZ4tspm2wnXLb1ThQA== -->";
//        return config;
//    }
}
