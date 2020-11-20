//package com.enuos.test;
//
//import com.enuos.live.SocialImUserApplication;
//import com.enuos.live.constants.RedisKey;
//import com.enuos.live.feign.ManageFeign;
//import com.enuos.live.mapper.UserMapper;
//import com.enuos.live.result.Result;
//import com.enuos.live.service.ExpService;
//import com.enuos.live.service.HandlerService;
//import com.enuos.live.task.TemplateEnum;
//import com.enuos.live.utils.RedisUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.geo.*;
//import org.springframework.data.redis.connection.RedisGeoCommands;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @Description
// * @Author wangyingjie
// * @Date 2020/8/3
// * @Modified
// */
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SocialImUserApplication.class)
//public class TestJunit {
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private RedisUtils redisUtils;
//
//    @Autowired
//    private ManageFeign manageFeign;
//
//    @Autowired
//    private HandlerService handlerService;
//
//    @Autowired
//    private UserMapper userMapper;
//
//    @Autowired
//    private ExpService expService;
//
//    /**
//     * @Description: 测试查询
//     * @Param: []
//     * @Return: void
//     * @Author: wangyingjie
//     * @Date: 2020/8/3
//     */
//    @Test
//    public void testSelectUser() {
//        List<Map<String, Object>> list = userMapper.blacklist(1L, 1);
//
//        Map<String, Object> userMap = userMapper.getUserBaseByUserId(121239443L);
//        System.out.println("userMap:" + userMap);
//    }
//
//    /**
//     * @Description: testInviteHandler
//     * @Param: []
//     * @Return: void
//     * @Author: wangyingjie
//     * @Date: 2020/8/7
//     */
//    @Test
//    public void testInviteHandler() {
//        handlerService.dailyTask(107061724L, 10, TemplateEnum.G01);
//    }
//
//    @Test
//    public void testUploadFileUrl() {
//        Result result = manageFeign.uploadFileUrl("http://thirdqq.qlogo.cn/g?b=oidb&k=WLEeBkYOwawT1eDcVxjZXg&s=100&t=1559289625", "header/");
//        System.out.println(result);
//    }
//
//    @Test
//    public void testRedisUtils() {
//    }
//
//    @Test
//    public void testUser() {
//        Map<String, Object> map = expService.level(1, 0L, 800L);
//        System.out.println(map);
//    }
//
//    @Test
//    public void testGEO() {
//        Double wd1 = 32.03074259383931;
//        Double jd1 = 118.7281774843562;
//
//        Double wd2 = 32.035386;
//        Double jd2 = 118.719012;
//
//        redisTemplate.opsForGeo().add(RedisKey.KEY_GEO, new Point(jd1, wd1), 1);
//        redisTemplate.opsForGeo().add(RedisKey.KEY_GEO, new Point(jd2, wd2), 2);
//
//        Distance distance = new Distance(5, Metrics.KILOMETERS);
//
//        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisUtils.getGeo(RedisKey.KEY_GEO, 1, distance);
//        // GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisUtils.getGeo(RedisKey.KEY_GEO, 1, distance, 1000L);
//
//        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoList = geoResults.getContent();
//
//        geoList.forEach(g -> System.out.println(g));
//    }
//
//    @Test
//    public void testTask() {
//        String key = "LOG_TASK_DAY:119178281:20201027";
//
//        redisUtils.incrHash(key, "L01", 1);
//        redisUtils.incrHash(key, "L02", 1);
//
//
//    }
//
//}
