package com.enuos.live.cipher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Description AES算法加解密
 * @Author wangyingjie
 * @Date 2020/9/7
 * @Modified
 */
@Slf4j
public class AESEncoder {

    /** 算法名 */
    private static final String NAME = "AES";

    /** 算法/模式/填充模式 */
    private static final String MODE = "AES/ECB/PKCS5Padding";

    /** 编码格式 */
    private static final String ENCODING = "utf-8";

    /** key */
    private static final String KEY = "eNuOs.";

    /**
     * @Description: 获取签名
     * @Param: [signature]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/9/7
     */
    private static String getKey(Long signature) {
        String key = KEY.concat(String.valueOf(signature));
        if (key.length() != 16) {
            key = addKey(key, "0", 16);
        }
        return key;
    }

    /**
     * @Description: 加密
     * @Param: [signature, data]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/9/7
     */
    public static String encrypt(Long signature, String data) {
        try {
            Cipher cipher = Cipher.getInstance(MODE);
            SecretKeySpec keySpec = new SecretKeySpec(getKey(signature).getBytes(ENCODING), NAME);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            log.error("AESEncoder encrypt error : {}", e.getMessage());
            return null;
        }
    }
    
    /** 
     * @Description: 解密
     * @Param: [signature, data] 
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/9/7 
     */ 
    public static String decrypt(Long signature, String data) {
        if (signature == null || StringUtils.isBlank(data)) {
            return "{}";
        }

        try {
            byte[] encrypted = Base64.decodeBase64(data.getBytes(ENCODING));
            Cipher cipher = Cipher.getInstance(MODE);
            SecretKeySpec keySpec = new SecretKeySpec(getKey(signature).getBytes(ENCODING), NAME);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypts = cipher.doFinal(encrypted);
            return new String(decrypts, ENCODING);
        } catch (Exception e) {
            log.error("AESEncoder decrypt error : {}", e.getMessage());
            return null;
        }
    }

    /** 
     * @Description: 补位
     * @Param: [key, item, length] 
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/9/16 
     */ 
    public static String addKey(String key, String item, int length) {
        int initLength = key.length();
        StringBuffer sb = new StringBuffer(key);
        while (initLength < length) {
            sb.append(item);//右补0
            initLength = sb.length();
        }
        return sb.substring(0, length);
    }

}
