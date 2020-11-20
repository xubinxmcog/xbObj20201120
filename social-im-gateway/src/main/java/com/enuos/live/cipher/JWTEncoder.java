package com.enuos.live.cipher;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description jwt加解密
 * @Author wangyingjie
 * @Date 2020/7/1
 * @Modified
 */
public class JWTEncoder {

    private static final String TOKEN_SECRET = "enuos";

    /**
     * @Description: 加密
     * @Param: [userId, token]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    public static String encode(String userId, String token) {
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        Map<String, Object> header = new HashMap<String, Object>(2) {
            {
                put("type", "JWT");
                put("alg", "HS256");
            }
        };

        return JWT.create().withHeader(header).withClaim("userId", userId).withClaim("token", token).sign(algorithm);
    }

    /**
     * @Description: 解密
     * @Param: [token]
     * @Return: com.auth0.jwt.interfaces.DecodedJWT
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    public static DecodedJWT decode(String token) {
        return JWT.decode(token);
    }
}
