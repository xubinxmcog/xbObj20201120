package com.enuos.live.config;

import com.enuos.live.interceptor.filter.TokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName GatewayConfig
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/2
 * @Version V1.0
 **/
@Configuration
public class GatewayConfig {

    @Bean
    public TokenFilter tokenFilter() {
//         CorsConfiguration corsConfiguration = new CorsConfiguration();
//         corsConfiguration.addAllowedHeader("*");
//         corsConfiguration.addAllowedOrigin("*");
//         corsConfiguration.addAllowedMethod("*");
//
//         UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource(new PathPatternParser());
//         urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new TokenFilter();
    }

//    @Bean
//    public WebFilter corsFilter() {
//        return (ServerWebExchange ctx, WebFilterChain chain) -> {
//            ServerHttpRequest request = ctx.getRequest();
//            if (CorsUtils.isCorsRequest(request)) {
//                ServerHttpResponse response = ctx.getResponse();
//                HttpHeaders headers = response.getHeaders();
//                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "");
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
//                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
//                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
//                if (request.getMethod() == HttpMethod.OPTIONS) {
//                    response.setStatusCode(HttpStatus.OK);
//                    return Mono.empty();
//                }
//            }
//            return chain.filter(ctx);
//        };
//    }

}
