package com.enuos.live.utils;

import java.security.MessageDigest;

/**
 * md5加密
 *
 * @author WangCaiWen
 * Created on 2020/4/24 9:31
 */
public class Md5 {

    /**
     * 标准MD5加密
     */
    public static String toMD5(String inStr) {
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(inStr.getBytes("UTF-8"));
            byte b[] = md.digest();
            int i;
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(i));
            }
        } catch (Exception e) {
            return null;
        }
        return sb.toString().toUpperCase();
    }
}
