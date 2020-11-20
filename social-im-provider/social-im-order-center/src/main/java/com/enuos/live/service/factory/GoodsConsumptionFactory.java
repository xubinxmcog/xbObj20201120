package com.enuos.live.service.factory;

import com.enuos.live.service.GoodsConsumption;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName GoodsConsumptionFactory
 * @Description: TODO 背包消费工厂
 * @Author xubin
 * @Date 2020/5/11
 * @Version V1.0
 **/
@Component
public class GoodsConsumptionFactory implements ApplicationContextAware {

    private static Map<String, GoodsConsumption> goodsConsumptionMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, GoodsConsumption> map = applicationContext.getBeansOfType(GoodsConsumption.class);
        goodsConsumptionMap = new HashMap();
        map.forEach((key, value) -> goodsConsumptionMap.put(value.getCode(), value));
    }

    public static <T extends GoodsConsumption> T getGoodsConsumption(String code) {
        return (T) goodsConsumptionMap.get(code);
    }
}
