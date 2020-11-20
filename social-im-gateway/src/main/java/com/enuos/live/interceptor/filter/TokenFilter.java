package com.enuos.live.interceptor.filter;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.cipher.JWTEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName TokenFilter
 * @Description: TODO gateway全局过滤器
 * @Author xubin
 * @Date 2020/4/2
 * @Version V1.0
 **/

@Slf4j
public class TokenFilter implements GlobalFilter, Ordered {

    private static final String USERID = "userId";

    private static final String TOKEN = "token";

    public static final String KEY_TOKEN = "KEY_TOKEN:";

    @Value("${whitelist.url}")
    private String whiteListUrl;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @Description: token过滤，headers中的toke为JWT加密后的token，解密可获得userId和源token，通过userId获取redis中的token，比较token是否一致。
     * @Param: [exchange, chain]
     * @Return: reactor.core.publisher.Mono<java.lang.Void>
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {

            ServerHttpRequest request = exchange.getRequest();
            String uri = request.getURI().getPath();
            String uti = interceptUti(uri);

            log.info("==========[Request [uri:{}]]==========", uri);

            if (whiteListUrl.contains(uti)) {
                return chain.filter(exchange);
            }

            String token = request.getHeaders().getFirst(TOKEN);

//            try { // 文件上传头像过滤
//                String value = request.getQueryParams().get("folder").get(0);
//                Mono<MultiValueMap<String, String>> formData = exchange.getFormData();
//                log.info(formData.toString());
//                if ("header".contains(value)) {
//                    return chain.filter(exchange);
//                }
//            } catch (Exception e) {
//            }

            if (StringUtils.isBlank(token)) {
                log.error("==========[token is null from headers]==========");
                return tips(exchange);
            }

            String userId = JWTEncoder.decode(token).getClaim(USERID).asString();
            String key = KEY_TOKEN.concat(userId);
//            if (!redisTemplate.hasKey(key)) {
//                log.error("==========[token is null from redis]==========");
//                return tips(exchange);
//            }

            if (!Objects.equals(token, redisTemplate.opsForValue().get(key))) {
                log.error("==========[token is different]==========");
                return tips(exchange);
            }

            ServerWebExchange mutatedExchange = exchange.mutate().request(request.mutate().headers(httpHeaders -> {
                httpHeaders.add(USERID, userId);
            }).build()).build();
            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("==========[token decode error]==========");
            return tips(exchange);
        }
    }

    /**
     * @Description: token验证失败
     * @Param: [exchange]
     * @Return: reactor.core.publisher.Mono<java.lang.Void>
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    private Mono<Void> tips(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        JSONObject result = new JSONObject(new LinkedHashMap());
        result.put("code", 1010);
        result.put("msg", "登录过期");
        byte[] datas = JSONObject.toJSONBytes(result);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(datas);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private String interceptUti(String uri) {
        Pattern pattern = Pattern.compile("/");
        Matcher findMatcher = pattern.matcher(uri);
        int number = 0;
        while (findMatcher.find()) {
            number++;
            if (number == 2) {// 当“/”第2次出现时停止
                break;
            }
        }
        int i = findMatcher.start();
        return uri.substring(i);
    }

}
