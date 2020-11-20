package com.enuos.live.service;

import java.security.PublicKey;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/1
 * @Modified
 */
public interface AppleService {

    /**
     * @Description: throws Exception
     * @Param: [kid]
     * @Return: java.security.PublicKey
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    PublicKey getPublicKey(String kid) throws Exception;
    
    /** 
     * @Description: 验证
     * @Param: [identityToken] 
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/6/1 
     */ 
    boolean verify(String identityToken);

}
