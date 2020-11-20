package com.enuos.test;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.cipher.AESEncoder;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.*;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/8
 * @Modified
 */
public class TestEncryptData {

    public static void main(String[] args) {
        String localDateTime = now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println(localDateTime);

        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put("local", localDateTime);
            }
        };

        System.out.println(AESEncoder.encrypt(1234567890L, JSONObject.toJSONString(map)));


        System.out.println(AESEncoder.decrypt(1601024581L,"qzt0QZxCsBLRYjHtlrtmBqnXpIGb1o4As7ggqd3UmWSD+nLcu9bfRWauc1NDK/bXJ280OXPa6jW6InJn1zxBaw=="));
    }
}
