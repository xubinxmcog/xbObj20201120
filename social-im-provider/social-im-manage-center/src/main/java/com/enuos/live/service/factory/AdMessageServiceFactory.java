package com.enuos.live.service.factory;

import com.enuos.live.common.constants.ReleaseTypeEnum;
import com.enuos.live.service.AdMessageService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName AdMessageServiceFactory
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/17
 * @Version V1.0
 **/
@Component
public class AdMessageServiceFactory implements ApplicationContextAware {

    private static Map<ReleaseTypeEnum, AdMessageService> adMessageServiceMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AdMessageService> map = applicationContext.getBeansOfType(AdMessageService.class);
        adMessageServiceMap = new HashMap<>();

        map.forEach((key, value) -> adMessageServiceMap.put(value.getCode(),value));
    }

    public static <T extends AdMessageService> T getAdMessageService(ReleaseTypeEnum code){
        return (T) adMessageServiceMap.get(code);
    }
}
