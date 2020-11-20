package com.enuos.live.job;

import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.feign.OrderFeign;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.UserCharmHonorMapper;
import com.enuos.live.pojo.TaskReward;
import com.enuos.live.pojo.UserCharmHonor;
import com.enuos.live.pojo.UserTitle;
import com.enuos.live.utils.TimeDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName UserHonorJob
 * @Description: TODO 用户排行榜刷新任务
 * @Author xubin
 * @Date 2020/9/3
 * @Version V2.0
 **/
@Slf4j
@Component
public class UserHonorJob {

    @Autowired
    private UserCharmHonorMapper userCharmHonorMapper;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private UserFeign userFeign;

    /**
     * @MethodName: yesterdayCharmDedicate
     * @Description: TODO 刷新昨日荣誉榜和守护榜 并发放奖励 更新荣誉室
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 10:52 2020/9/3
     **/
    // 每天0点三分执行
    @Scheduled(cron = "0 3 0 * * ?")// 每天0点三分执行
//    @Scheduled(cron = "0 0/1 * * * ?") // 每2分钟执行一次
    public void yesterdayCharmDedicate() {
        log.info("开始刷新昨日荣誉榜和守护榜 并发放奖励 更新荣誉室");

        String startTime = TimeDateUtils.getYesterdayZeroPoint(); // 开始时间 昨日0:0:0
        String endTime = TimeDateUtils.getYesterdayLastSecond(); // 结束时间 昨日23:59:29
        String charm = "user_id"; // 魅力(魅力值)
        String guard = "give_user_id"; // 守护(贡献值)
        Integer pageSize = 20; // 查询20条

        // 魅力(魅力值) 处理
        List<UserCharmHonor> charms = userCharmHonorMapper.getCharmDedicate(charm, startTime, endTime, pageSize);
        if (ObjectUtil.isNotEmpty(charms)) {

            int size = charms.size();
            // 获取第一名保存到荣誉室
            UserCharmHonor honor = charms.get(0);
            honor.setCharmValue(honor.getTotalValue());
            honor.setType(1);
            int insert = userCharmHonorMapper.insert(honor);
            log.info("魅力(魅力值)获取第一名保存到荣誉室,status=[{}]", insert);

            // 奖励发放
            List<TaskReward> taskRewards = userCharmHonorMapper.getTaskRewards("RankingCharm"); // 获取奖励配置
            Map<Integer, List<TaskReward>> map = taskRewards.stream().collect(Collectors.groupingBy(TaskReward::getSuffix));
            List<TaskReward> rewards = null;
            for (int i = 0; i < size; i++) {
                if (map.containsKey(i + 1)) {
                    rewards = map.get(i + 1);
                }
                UserCharmHonor honor1 = charms.get(i);
                Long userId = honor1.getUserId();
                Map<String, Object> param = new HashMap<>();
                for (int i1 = 0; i1 < rewards.size(); i1++) {
                    TaskReward taskReward = rewards.get(i1);
                    String rewardCode = taskReward.getRewardCode();
                    param.put("userId", userId);
                    List<TaskReward> list = new ArrayList<>();
                    list.add(taskReward);
                    param.put("list", list);
                    if (rewardCode.startsWith("CH")) { // 发放称号
                        log.info("魅力(魅力值)发放称号, userId=[{}]", userId);
                        upUserTitle(userId, taskReward);
                    } else if (rewardCode.startsWith("T")) { // 发放券
                        log.info("魅力(魅力值)发放券, userId=[{}]", userId);
                        orderFeign.addBackpack(param);
                    } else if (rewardCode.startsWith("JC")) { // 发放进场特效
                        log.info("魅力(魅力值)发放进场特效, userId=[{}]", userId);
                        orderFeign.addBackpack(param);
                    } else if ("GOLD".equals(rewardCode)) { // 发放金币
                        log.info("魅力(魅力值)发放金币, userId=[{}]", userId);
                        userFeign.rewardHandler(param);
                    }
                }

            }

        }

        // 守护(贡献值)处理
        List<UserCharmHonor> guards = userCharmHonorMapper.getCharmDedicate(guard, startTime, endTime, pageSize);
        if (ObjectUtil.isNotEmpty(guards)) {
            int size = guards.size();
            // 获取第一名保存到荣誉室
            UserCharmHonor dedicate = guards.get(0);
            dedicate.setCharmValue(dedicate.getTotalValue());
            dedicate.setType(2);
            int insert = userCharmHonorMapper.insert(dedicate);
            log.info("守护(贡献值)获取第一名保存到荣誉室,status=[{}]", insert);

            // 奖励发放
            List<TaskReward> taskRewards = userCharmHonorMapper.getTaskRewards("RankingGuard"); // 获取奖励配置
            Map<Integer, List<TaskReward>> map = taskRewards.stream().collect(Collectors.groupingBy(TaskReward::getSuffix));

            List<TaskReward> rewards = null;
            for (int i = 0; i < size; i++) {
                if (map.containsKey(i + 1)) {
                    rewards = map.get(i + 1);
                }
                UserCharmHonor honor = guards.get(i);
                Long userId = honor.getUserId();
                Map<String, Object> param = new HashMap<>();
                for (int i1 = 0; i1 < rewards.size(); i1++) {
                    TaskReward taskReward = rewards.get(i1);
                    String rewardCode = taskReward.getRewardCode();
                    param.put("userId", userId);
                    List<TaskReward> list = new ArrayList<>();
                    list.add(taskReward);
                    param.put("list", list);
                    if (rewardCode.startsWith("CH")) { // 发放称号
                        log.info("守护(贡献值)发放称号, userId=[{}]", userId);
                        upUserTitle(userId, taskReward);
                    } else if (rewardCode.startsWith("T")) { // 发放券
                        log.info("守护(贡献值)发放券, userId=[{}]", userId);
                        orderFeign.addBackpack(param);
                    } else if (rewardCode.startsWith("JC")) { // 发放进场特效
                        log.info("守护(贡献值)发放进场特效, userId=[{}]", userId);
                        orderFeign.addBackpack(param);
                    } else if ("GOLD".equals(rewardCode)) { // 发放金币
                        log.info("守护(贡献值)发放金币, userId=[{}]", userId);
                        userFeign.rewardHandler(param);
                    }
                }
            }
        }
    }


    /**
     * @MethodName: upUserTitle
     * @Description: TODO 处理发放称号
     * @Param: [userId, rewards]
     * @Return: void
     * @Author: xubin
     * @Date: 17:25 2020/9/3
     **/
    public void upUserTitle(Long userId, TaskReward reward) {

        String rewardCode = reward.getRewardCode();// 奖品code
        Long life = reward.getLife(); // 有效期限 [秒]
        if (life == 0) {
            life = 3153600000L;
        }
        Integer number = Integer.valueOf(reward.getNumber());

        UserTitle userTitle1 = userCharmHonorMapper.getUserTitle(userId, rewardCode);
        if (ObjectUtil.isEmpty(userTitle1)) {
            Date date = new Date();
            long milliSecond = System.currentTimeMillis() + (life * 1000 * number);
            date.setTime(milliSecond);
            UserTitle title = new UserTitle();
            title.setUserId(userId);
            title.setTitleCode(rewardCode);
            title.setExpireTime(date);
            int i = userCharmHonorMapper.insertUserTitle(title);
            if (i != 1) {
                log.error("发放称号失败, userId=[{}]", userId);
            }
        } else {
            Date expireTime = userTitle1.getExpireTime();
            Date date = new Date();
            int compareTo = expireTime.compareTo(date);
            if (compareTo == -1) {
                long milliSecond = System.currentTimeMillis() + (life * 1000 * number);
                date.setTime(milliSecond);
            } else {
                long milliSecond = expireTime.getTime() + (life * 1000 * number);
                date.setTime(milliSecond);
            }
            userTitle1.setExpireTime(date);
            int i = userCharmHonorMapper.updateUserTitle(userTitle1);
            if (i != 1) {
                log.error("发放称号更新失败, userId=[{}]", userId);
            }

        }


    }

    public static void main(String[] args) {

        String beginTime = "2021-07-28 14:42:32";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date date1 = format.parse(beginTime);
            date1.getTime();
            System.out.println(date1.getTime());

            int compareTo = date1.compareTo(new Date());

            System.out.println(compareTo);

            LocalDate date = LocalDate.now();
            System.out.println("当前日期=" + date);

            LocalTime time = LocalTime.now();
            LocalTime newTime = time.plus(259200, ChronoUnit.NANOS);
            System.out.println("newTime=" + newTime);

            LocalDateTime now = LocalDateTime.now();
            now.plus(259200, ChronoUnit.SECONDS);
            System.out.println(now);
        } catch (ParseException e) {
            e.printStackTrace();

        }
    }
}
