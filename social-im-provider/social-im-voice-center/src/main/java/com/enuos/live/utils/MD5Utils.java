package com.enuos.live.utils;

import java.security.MessageDigest;

public class MD5Utils {
    /**
     * 返回32位大写的MD5加密结果 <br>
     * @param plainText 明文
     */
    public static String encrypt(String plainText) throws Exception {
        char[] charArray = plainText.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
        {
            byteArray[i] = (byte) charArray[i];
        }

        MessageDigest md5Instance = null;
        try
        {
            md5Instance = MessageDigest.getInstance("MD5");
        }
        catch (Exception e)
        {
            throw new Exception("MD5加密算法实例化失败", e);
        }

        byte[] md5Bytes = md5Instance.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++)
        {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
            {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString().toUpperCase();
    }

    /**
     * MD5加密后取8位特殊字符作为加密结果 <br>
     */
    public static String encrypt8(String plainText)
    {
        String md5 = null;
        try {
            md5 = MD5Utils.encrypt(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5.substring(0, 2) + md5.substring(14, 18) + md5.substring(30);
    }
}
