package com.enuos.live.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.CommonMapper;
import com.enuos.live.pojo.Account;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;
import com.enuos.live.service.CommonService;
import com.enuos.live.service.HandlerService;
import com.enuos.live.task.Task;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.AgeUtil;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.QRCodeUtils;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 通用业务
 * @Author wangyingjie
 * @Date 12:57 2020/4/14
 * @Modified
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private HandlerService handlerService;

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Resource(name = "asyncServiceExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * @Description: 标签
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result label() {
        List<Map<String, Object>> list = commonMapper.getLabel();
        if (CollectionUtils.isEmpty(list)) {
            log.error("Label is null");
            return Result.error(ErrorCode.NO_DATA);
        }

        // 结果分类重定义key值 2=roomTheme；3=roomBannedType；4=roomSeatType；5=gameSort
        Map<String, List<Map<String, Object>>> map = list.stream().collect(Collectors.groupingBy(m -> {
            switch (MapUtils.getIntValue(m, "category")) {
                case 2:
                    return "roomTheme";
                case 3:
                    return "roomBannedType";
                case 4:
                    return "roomSeatType";
                case 5:
                    return "gameSort";
                default:
                    return "";
            }
        }));

        return Result.success(map);
    }

    /**
     * @Description: 刷新登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/28
     */
    @Override
    public Result refreshLogin(Account account) {
        log.info("Refresh login......");

        Long userId = account.userId;
        if (Objects.isNull(userId)) {
            log.error("Refresh login failed, userId is null");
            return Result.error();
        }

        // 刷新token
        redisUtils.expire(RedisKey.KEY_TOKEN.concat(String.valueOf(userId)), 7, TimeUnit.DAYS);

        // TASK[TD]：登陆日常
        threadPoolTaskExecutor.submit(() -> handlerService.dailyTask(new Task(TemplateEnum.L01, userId)));

        log.info("Refresh login success");
        return Result.success();
    }

    /**
     * @Description: 刷新位置
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result refreshPoint(Account account) {
        Long userId = account.userId;

        log.info("Refresh login point [userId: {}, longitude: {}, latitude: {}]", userId, account.getLongitude(), account.getLatitude());

        Double longitude = account.getLongitude();
        Double latitude = account.getLatitude();
        if (longitude != null && latitude != null && longitude >= -180 && longitude <= 180 && latitude >= -85.05112878 && latitude <= 85.05112878) {
            // 刷新位置
            redisUtils.setGeo(RedisKey.KEY_GEO, new Point(longitude, latitude), String.valueOf(userId));
        }

        return Result.success();
    }

    /**
     * @Description: 获取用户基本信息
     * @Param: [userId, keys]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Map<String, Object> getUserBase(Long userId, String... keys) {
        if (Objects.isNull(userId)) {
            return null;
        }

        Map<String, Object> userBaseMap = commonMapper.getUserBaseByUserId(userId);

        if (MapUtils.isEmpty(userBaseMap)) {
            return null;
        }

        userBaseMap.put("onLineStatus", getOnLineStatus(userId));
        userBaseMap.put("backgroundList", commonMapper.getUserBackgroundByUserId(userId));

        setAge(userBaseMap);

        if (ArrayUtils.isEmpty(keys)) {
            return userBaseMap;
        } else {
            // 过滤数据,仅返回需要的数据
            List<String> keyList = Arrays.asList(keys);
            return userBaseMap.entrySet().stream().filter(map -> keyList.contains(map.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    /**
     * @Description: 刷新在线状态
     * @Param: [user]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Map<String, Object> refreshOnLineStatus(User user) {
        Long userId = user.getUserId();
        Integer onLineStatus = user.getOnLineStatus();

        log.info("Refresh on line status [userId: {}, onLineStatus: {}]", userId, onLineStatus);

        if (!Objects.isNull(onLineStatus)) {
            redisUtils.setHash(RedisKey.KEY_ONLINE, String.valueOf(userId), onLineStatus);
        }

        return new HashMap<String, Object>(2) {
            {
                put("userId", userId);
                put("onLineStatus", getOnLineStatus(userId));
            }
        };

    }

    /**
     * @Description: 获取在线状态
     * @Param: [userId]
     * @Return: java.lang.Object
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Object getOnLineStatus(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }

        String redisKey = String.valueOf(userId);

        return redisUtils.hasHashKey(RedisKey.KEY_ONLINE, redisKey) ? redisUtils.getHash(RedisKey.KEY_ONLINE, redisKey) : 0;
    }

    /**
     * @Description: 获取二维码
     * @Param: [request, response]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public void getUserQRCode(HttpServletRequest request, HttpServletResponse response) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        String iconUrl = MapUtils.getString(getUserBase(userId, "iconUrl"), "iconUrl");
        JSONObject jsonObject = new JSONObject() {
            {
                put("userId", userId);
            }
        };

        QRCodeUtils.createImage(jsonObject.toJSONString(), iconUrl, response);
    }

    /** [PRIVATE] */

    /**
     * @Description: 设置年龄
     * @Param: [userBaseMap]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    private void setAge(Map<String, Object> userBaseMap) {
        userBaseMap.put("age", AgeUtil.getAge(DateUtils.parse(MapUtils.getString(userBaseMap, "birth"))));
    }

}
