package com.enuos.live.service.factory;

import com.enuos.live.service.UpPetsHandleService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName UpPetsHandleFactory
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/12
 * @Version V2.0
 **/
@Component
public class UpPetsHandleFactory implements ApplicationContextAware {

    private static Map<String, UpPetsHandleService> upPetsMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, UpPetsHandleService> map = applicationContext.getBeansOfType(UpPetsHandleService.class);

        upPetsMap = new HashMap<>();

        map.forEach((key, value) -> upPetsMap.put(value.getCode(), value));

    }

    public static <T extends UpPetsHandleService> T getUpPetsHandleSevice(String code) {
        return (T) upPetsMap.get(code);
    }
}
