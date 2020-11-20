package com.enuos.live.containers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName Container
 * @Description: TODO
 * @Author xubin
 * @Date 2020/9/17
 * @Version V2.0
 **/
public class ServiceContainer {

    public static Map<String, Long> serviceMap = new ConcurrentHashMap<>(16);

    public static Map<String, Long> getServiceMap() {
        return serviceMap;
    }
}
