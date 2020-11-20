package com.enuos.live.controller;

import com.enuos.live.mapper.ExceptionInfoMapper;
import com.enuos.live.pojo.ExceptionServiceInfo;
import com.enuos.live.service.MailService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TestController
 * @Description: TODO
 * @Author xubin
 * @Date 2020/9/14
 * @Version V2.0
 **/
@Api("发邮件")
@Slf4j
@RestController
@RequestMapping("/main")
public class TestController {

    @Autowired
    MailService mailService;

    @Autowired
    ExceptionInfoMapper exceptionInfoMapper;

    @PostMapping("/send")
    public void sendSimpleMail(String to, String subject, String contnet) {
        mailService.sendSimpleMail(to, subject, contnet);
    }

    @PostMapping("/htmlSend")
    public void sendHtmlMail(String to, String subject, String contnet) {

        contnet = "[<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg><appid><![CDATA[wxdfbd92887c7d7a81]]></appid><mch_id><![CDATA[1601105314]]></mch_id><nonce_str><![CDATA[dNugtV129kEW7NLk]]></nonce_str><sign><![CDATA[D64B2626827C0E81960635D53D205575]]></sign><result_code><![CDATA[SUCCESS]]></result_code><prepay_id><![CDATA[wx07140347876078631ab30f699088410000]]></prepay_id><trade_type><![CDATA[APP]]></trade_type></xml>]\n" +
                "微信统一下单返回结果解析=[{nonce_str=dNugtV129kEW7NLk, appid=wxdfbd92887c7d7a81, sign=D64B2626827C0E81960635D53D205575, trade_type=APP, return_msg=OK, result_code=SUCCESS, mch_id=1601105314, return_code=SUCCESS, prepay_id=wx07140347876078631ab30f699088410000}]";
        mailService.sendHtmlMail(to, subject, contnet);
    }

    @GetMapping("insert")
    public int insert(){
        List<ExceptionServiceInfo> records = new ArrayList();
        for (int i = 0; i < 3; i++) {
            ExceptionServiceInfo record = new ExceptionServiceInfo();
            record.setName("SOCIAL-IM-VOICE");
            record.setIp("192.168.0.6"+i);
            record.setLastConnectionTime("2020-09-18 14:46:00");
            records.add(record);
        }

        return exceptionInfoMapper.insertServiceException(records);
    }
}
