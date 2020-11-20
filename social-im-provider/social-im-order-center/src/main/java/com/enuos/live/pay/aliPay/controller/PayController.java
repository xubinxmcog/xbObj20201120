package com.enuos.live.pay.aliPay.controller;

import cn.hutool.core.map.MapUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.pay.aliPay.AlipayTrade;
import com.enuos.live.pay.aliPay.constants.AliPayConstants;
import com.enuos.live.pojo.Params;
import com.enuos.live.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @ClassName PayController
 * @Description: TODO 支付宝支付
 * @Author xubin
 * @Date 2020/4/27
 * @Version V1.0
 **/
@RestController
@RequestMapping("/payment/aliPay")
@Slf4j
public class PayController {

    @Autowired
    private AlipayTrade alipayTrade;

//    @Cipher
    @ApiOperation("手机网站支付")
    @RequestMapping("/phoneWeb")
    public void phoneWeb(@RequestParam Map<String, String> sParaTemp, HttpServletResponse response) throws IOException {
        log.info("支付宝手机网站支付入参：【{}】", sParaTemp);
        sParaTemp.put("productCode", "QUICK_WAP_WAY");
        alipayTrade.aliPayWapRequest(sParaTemp, response);
    }

//    @Cipher
    @ApiOperation("网站支付")
    @RequestMapping(value = "/pcWeb")
    public void pcWeb(@RequestParam Map<String, String> sParaTemp, HttpServletResponse response) throws IOException {
        log.info("支付宝网站支付入参：【{}】", sParaTemp);
        sParaTemp.put("productCode", "FAST_INSTANT_TRADE_PAY");
        alipayTrade.alipayWeb(sParaTemp, response);
    }

    @Cipher
    @ApiOperation("APP支付")
    @RequestMapping(value = "/app")
    public Result tradeAppPayRequest(@RequestBody Map<String, String> params, HttpServletResponse response) {
        log.info("支付宝APP支付入参：【{}】", params);
//        Map body = sParaTemp.getBody();
        return alipayTrade.tradeAppPayRequest(params);
    }

    @Cipher
    @ApiOperation("支付查询")
    @RequestMapping(value = "/query")
    public Result aliPayTradeQuery(@RequestBody Map<String, String> params) {
        String orderNo = MapUtil.getStr(params, "orderNo");
        return Result.success(alipayTrade.aliPayTradeQuery(orderNo));
    }

    /**
     * 参数：类型：是否必填：最大长度：描述：示例
     * body：String：否 ：128：对一笔交易的具体描述信息。如果是多种商品，请将商品描述字符串累加传给body。：Iphone6 16G
     * subject：String：是 ：256：商品的标题/交易标题/订单标题/订单关键字等。：大乐透
     * out_trade_no：String：是：64：商户网站唯一订单号：20200428010101003
     * timeout_express：String：否：6：该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m。注意：此时间为创建订单成功后开始计时的时间；若为空，则默认为15d。：90m
     * time_expire：String：否：32：绝对超时时间，格式为yyyy-MM-dd HH:mm。 注：1）以支付宝系统时间为准；2）如果和timeout_express参数同时传入，以time_expire为准。：2020-04-30 10:05
     * total_amount：Price：是：9：订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。：9:00
     * auth_token：String：否：40：针对用户授权接口，获取用户相关数据时，用于标识用户授权关系 注：若不属于支付宝业务经理提供签约服务的商户，暂不对外提供该功能，该参数使用无效。
     * product_code：String：是：64：销售产品码，商家和支付宝签约的产品码。该产品请填写固定值：QUICK_WAP_WAY。：QUICK_WAP_WAY
     * goods_type：String：否：2：商品主类型：0—虚拟类商品，1—实物类商品 注：虚拟类商品不支持使用花呗渠道。：0
     * passback_params：String：否：公用回传参数，如果请求时传递了该参数，则返回给商户时会回传该参数。支付宝会在异步通知时将该参数原样返回。本参数必须进行UrlEncode之后才可以发送给支付宝：
     *
     * @param httpRequest
     * @param httpResponse
     * @throws ServletException
     * @throws IOException
     */
    /**
     * @MethodName: signVerified
     * @Description: TODO 异步通知验签
     * @Param: [sign, request, response]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/4/30
     **/
    @RequestMapping("/signVerified")
    public void signVerified(@RequestParam Map params, HttpServletRequest request,
                             HttpServletResponse response) throws AlipayApiException, IOException {

        Boolean verification = alipayTrade.synCnoticeVerification(params);
        if (verification) {
            response.getWriter().write("success");
        } else {
            response.getWriter().write("failure");
        }
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * @MethodName: synCnoticeVerification
     * @Description: TODO 异步通知验签
     * @Param: [request, response]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/4/30
     **/
    @PostMapping("/synCV")
    public void synCnoticeVerification(HttpServletRequest request,
                                       HttpServletResponse response) throws AlipayApiException {
        Map<String, String> paramsMap = new TreeMap<>(); //将异步通知中收到的所有参数都存放到map中
        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach((key, value) -> paramsMap.put(key, value[0]));

        boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, AliPayConstants.ALIPAY_PUBLIC_KEY, AliPayConstants.CHARSET, AliPayConstants.SIGN_TYPE); //调用SDK验证签名

        try {
            if (signVerified) {
                // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                String verification = "success";/*paymentService.synCnoticeVerification(paramsMap);*/
                System.out.println("支付成功！");
                response.getWriter().write(verification);
            } else {
                // TODO 验签失败则记录异常日志，并在response中返回failure.
                log.info("支付失败");
                response.getWriter().write("failure");
            }
//            response.getWriter().flush();
//            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
