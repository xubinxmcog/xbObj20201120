package com.enuos.live.service.factory;

import com.enuos.live.common.constants.ReportTypeEnum;
import com.enuos.live.service.TipOffsService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName TipOffsServiceFactory
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/15
 * @Version V1.0
 **/
@Component
public class TipOffsServiceFactory implements ApplicationContextAware {

    private static Map<String, TipOffsService> tipOffsServiceMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, TipOffsService> map = applicationContext.getBeansOfType(TipOffsService.class);
        tipOffsServiceMap = new HashMap<>();
        map.forEach((key, value) -> tipOffsServiceMap.put(value.getCode(), value));
    }

    public static <T extends TipOffsService> T getTipOffsService(String code){
        return (T) tipOffsServiceMap.get(code);
    }
}
