package com.enuos.live.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Description RedisUtils
 * @Author wangyingjie
 * @Date 2020/10/19
 * @Modified
 */
@Slf4j
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate redisTemplate;

    /* ============================================================[Common]============================================================ */

    /**
     * @Description: 模糊查询获取key值
     * @Param: [pattern]
     * @Return: java.util.Set
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public Set keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * @Description: 使用Redis的消息队列
     * @Param: [channel, message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public void convertAndSend(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    /**
     * @Description: 指定缓存失效时间
     * @Param: [key, time]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 指定缓存失效时间
     * @Param: [key, time, unit]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public boolean expire(String key, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, unit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 根据key 获取过期时间
     * @Param: [key]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * @Description: 判断key是否存在
     * @Param: [key]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 移除KEY
     * @Param: [key]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    /**
     * @Description: 移除KEY
     * @Param: [key]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public void remove(String... keys) {
        if (keys != null && keys.length > 0) {
            if (keys.length == 1) {
                redisTemplate.delete(keys[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(keys));
            }
        }
    }

    /**
     * @Description: 移除KEY
     * @Param: [keyList]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public void remove(List<Integer> keyList) {
        redisTemplate.delete(keyList);
    }

    /* ============================================================[String]============================================================ */

    /**
     * @Description: 普通缓存获取
     * @Param: [key]
     * @Return: java.lang.Object
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * @Description: 普通缓存放入
     * @Param: [key, value]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 普通缓存放入并设置时间
     * @Param: [key, value, time]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 
     * @Description: 普通缓存放入并设置时间
     * @Param: [key, value, time, timeUnit] 
     * @Return: boolean 
     * @Author: wangyingjie
     * @Date: 2020/10/20 
     */ 
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 递增
     * @Param: [key, delta]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * @Description: 递减
     * @Param: [key, delta]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    /* ============================================================[Hash]============================================================ */

    /**
     * @Description: 获取hash表中的值
     * @Param: [key, item]
     * @Return: java.lang.Object
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public Object getHash(String key, Object item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * @Description: 获取hash表中大key的所有键值对
     * @Param: [key]
     * @Return: java.util.Map<java.lang.Object,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    public Map<Object, Object> getMHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * @Description: 向一张hash表中放入数据, 如果不存在将创建
     * @Param: [key, item, value]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public boolean setHash(String key, Object item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: HASH
     * @Param: [key, item, value, time, timeUnit]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public boolean setHash(String key, String item, Object value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: HASH
     * @Param: [key, map]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean setMHash(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: HASH
     * @Param: [key, map, time]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean setMHash(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 递增
     * @Param: [key, item, by]
     * @Return: double
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public double incrHash(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * @Description: 递增
     * @Param: [key, item, by]
     * @Return: double
     * @Author: wangyingjie
     * @Date: 2020/10/28
     */
    public long incrHash(String key, String item, long by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * @Description: 递减
     * @Param: [key, item, by]
     * @Return: double
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public double decrHash(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    /**
     * @Description: 判断hash表中是否有该项的值
     * @Param: [key, item]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public boolean hasHashKey(String key, Object item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * @Description: 删除hash表中的值
     * @Param: [key, item]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public void delHash(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /* ============================================================[Set]============================================================ */

    /**
     * @Description: 根据key获取Set中的所有值
     * @Param: [key]
     * @Return: java.util.Set<java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public Set<Object> getSet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Description: 获取set缓存的长度
     * @Param: [key]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long getSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @Description: 根据value从一个set中查询, 是否存在
     * @Param: [key, value]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean hasSetKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 将数据放入set缓存
     * @Param: [key, values]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long setSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @Description: 将数据放入set缓存
     * @Param: [key, time, values]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long setSet(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @Description: 移除值为value的
     * @Param: [key, values]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long removeSet(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* ============================================================[List]============================================================ */

    /**
     * @Description: 获取list缓存的内容
     * @Param: [key, start, end]
     * @Return: java.util.List<java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public List<Object> getList(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Description: 获取list缓存的长度
     * @Param: [key]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long getListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @Description: 获取list中的值
     * @Param: [key, index]
     * @Return: java.lang.Object
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public Object getIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Description: 将list放入缓存
     * @Param: [key, value]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean setRightList(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 将list放入缓存
     * @Param: [key, value, time]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean setRightList(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 将list放入缓存
     * @Param: [key, value]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean setRightList(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 将list放入缓存
     * @Param: [key, value, time]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean setRightList(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 根据索引修改list中的某条数据
     * @Param: [key, index, value]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public boolean updateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 移除N个值为value
     * @Param: [key, count, value]
     * @Return: long
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* ============================================================[BoundListOperations]============================================================ */

    /**
     * @Description: 根据起始结束序号遍历Redis中的list
     * @Param: [listKey, start, end]
     * @Return: java.util.List<java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public List<Object> rangeList(String listKey, long start, long end) {
        //绑定操作
        BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
        //查询数据
        return boundValueOperations.range(start, end);
    }

    /**
     * @Description: 弹出右边的值并且移除这个值
     * @Param: [listKey]
     * @Return: java.lang.Object
     * @Author: wangyingjie
     * @Date: 2020/10/20
     */
    public Object rifhtPop(String listKey) {
        //绑定操作
        BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
        return boundValueOperations.rightPop();
    }

    /* ============================================================[Geo]============================================================ */

    /**
     * @Description: add geo
     * @Param: [key, point, value]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public boolean setGeo(String key, Point point, Object value) {
        try {
            redisTemplate.opsForGeo().add(key, point, value);
            return true;
        } catch (Exception e) {
            log.error("Invalid latitude and longitude");
            return false;
        }
    }

    /**
     * @Description: 获取范围distance内的limit个
     * @Param: [key, value, distance, limit]
     * @Return: org.springframework.data.geo.GeoResults<org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation   <   java.lang.String>>
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> getGeo(String key, Object value, Distance distance, Long limit) {
        try {
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .includeDistance().includeCoordinates().sortAscending().limit(limit);
            return redisTemplate.opsForGeo().radius(key, value, distance, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Description: 获取范围内的所有
     * @Param: [key, value, distance]
     * @Return: org.springframework.data.geo.GeoResults<org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation       <       java.lang.String>>
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> getGeo(String key, Object value, Distance distance) {
        try {
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .includeDistance().includeCoordinates().sortAscending();
            return redisTemplate.opsForGeo().radius(key, value, distance, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
