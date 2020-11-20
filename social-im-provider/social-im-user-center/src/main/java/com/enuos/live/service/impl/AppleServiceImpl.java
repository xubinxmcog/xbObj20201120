package com.enuos.live.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwk.Jwk;
import com.enuos.live.feign.AppleFeign;
import com.enuos.live.service.AppleService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;
import java.util.Objects;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/1
 * @Modified
 */
@Slf4j
@Service
public class AppleServiceImpl implements AppleService {

    @Autowired
    private AppleFeign appleFeign;

    /**
     * @Description: 获取公钥
     * @Param: [kid]
     * @Return: java.security.PublicKey
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public PublicKey getPublicKey(String kid) throws Exception {
        JSONObject jsonObject = appleFeign.authKeys();
        if (Objects.isNull(jsonObject)) {
            throw new RuntimeException("Apple can not get auth keys");
        }

        log.info("Apple get auth keys is [{}]", jsonObject);

        JSONArray jsonArray = jsonObject.getJSONArray("keys");

        // 根据kid去取正确的
        Object o = jsonArray.stream().filter(key -> kid.equals(JSONObject.parseObject(JSONObject.toJSONString(key)).getString("kid"))).findFirst().get();

        if (Objects.isNull(o)) {
            throw new RuntimeException("Apple can not get auth keys");
        }

        Jwk jwk = Jwk.fromValues(JSONObject.parseObject(JSONObject.toJSONString(o)).toJavaObject(Map.class));
        return jwk.getPublicKey();
    }

    /**
     * @Description: 验证
     * @Param: [identityToken]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @Override
    public boolean verify(String identityToken) {
        String[] strArray = identityToken.split("\\.");
        String kid = JSONObject.parseObject(new String(Base64.decodeBase64(strArray[0]))).getString("kid");

        log.info("Decode kid is [{}]", kid);

        try {
            if (strArray.length > 1) {
                String decode = new String(Base64.decodeBase64(strArray[1]));
                String aud = JSONObject.parseObject(decode).getString("aud");
                String sub = JSONObject.parseObject(decode).getString("sub");

                JwtParser jwtParser = Jwts.parser().setSigningKey(getPublicKey(kid));
                jwtParser.requireIssuer("https://appleid.apple.com");
                jwtParser.requireAudience(aud);
                jwtParser.requireSubject(sub);

                Jws<Claims> claim = jwtParser.parseClaimsJws(identityToken);
                if (claim != null && claim.getBody().containsKey("auth_time")) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Apple login verify error: [{}]", e);
        }

        return false;
    }

}
