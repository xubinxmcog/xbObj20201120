package com.enuos.live.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName BASE64Util
 * @Description: TODO base64 加密解密工具
 * @Author xubin
 * @Date 2020/7/20
 * @Version V2.0
 **/
public class BASE64Util {

    private static final BASE64Encoder encoder = new BASE64Encoder();
    private static final BASE64Decoder decoder = new BASE64Decoder();

    /**
     * @MethodName: cipher
     * @Description: TODO 编码
     * @Param: [text]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 11:23 2020/7/20
     **/
    public static String encoder(String text) {

        try {
            final byte[] textByte = text.getBytes("UTF-8");
            return encoder.encode(textByte);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @MethodName: cipher
     * @Description: TODO 解码
     * @Param: [text]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 11:23 2020/7/20
     **/
    public static String decoder(String encodedText) {

        try {
            return new String(decoder.decodeBuffer(encodedText), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
