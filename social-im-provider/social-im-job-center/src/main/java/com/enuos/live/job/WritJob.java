package com.enuos.live.job;

import com.enuos.live.mapper.WritMapper;
import com.enuos.live.pojo.Title;
import com.enuos.live.pojo.Writ;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 发放令状排行榜奖励
 * @Author wangyingjie
 * @Date 2020/10/15
 * @Modified
 */
@Slf4j
@Component
public class WritJob {

    @Autowired
    private WritMapper writMapper;

    /**
     * @Description: 每周一零点统计
     * @Param: []
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/15
     */
    @Scheduled(cron = "0 0 0 ? * MON")
    // @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void rankReward() {
        log.info("==========[Writ rank reward begin]");
        LocalDate currentDate = DateUtils.getCurrentDate();

        // 获取上一期令状
        Writ writ = writMapper.getWrit(TemplateEnum.A04.CODE, currentDate);
        if (Objects.isNull(writ)) {
            log.info("==========[Writ is null]");
            return;
        }

        // 非同一天则未结束
        if (!currentDate.equals(writ.getEndTime())) {
            log.info("==========[Writ do not end]");
            return;
        }

        // 同一天
        String taskCode = writ.getTaskCode();
        // 获取排行榜前十的用户ID
        List<Long> rankUserList = writMapper.getRankUser(taskCode, 10);
        if (CollectionUtils.isEmpty(rankUserList)) {
            log.warn("==========[Rank no user, writCode is [{}]]", taskCode);
            return;
        }

        // 获取奖励
        List<Map<String, Object>> rankRewardList = writMapper.getRankReward(taskCode, TemplateEnum.RANK.CODE);
        if (CollectionUtils.isEmpty(rankRewardList)) {
            log.warn("==========[Rank no reward, writCode is [{}]]", taskCode);
            return;
        }

        Map<Integer, Map<String, Object>> rankRewardMap = rankRewardList.stream().collect(Collectors.toMap(k -> MapUtils.getInteger(k, "suffix"), v -> v, (k1, k2) -> k1));
        long userId, life = 0;
        int rank, number;
        String rewardCode = null;
        for (int i = 0; i < rankUserList.size(); i++) {
            userId = rankUserList.get(i);
            rank = i + 1;
            if (rankRewardMap.containsKey(rank)) {
                rewardCode = MapUtils.getString(rankRewardMap.get(rank), "rewardCode");
                number = Optional.ofNullable(MapUtils.getIntValue(rankRewardMap.get(rank), "number")).orElse(0);
                life = Optional.ofNullable(MapUtils.getLongValue(rankRewardMap.get(rank), "life")).orElse(3153600000L) * number;
            }

            if (rewardCode == null) {
                continue;
            }

            Title title = writMapper.getTitle(userId, rewardCode);
            if (Objects.isNull(title)) {
                title = new Title(userId, rewardCode, DateUtils.getCurrentDateTime().plusSeconds(life));

                log.info("==========[Rank save {}]", title);
                writMapper.saveTitle(title);
            } else {
                title.setExpireTime(title.getExpireTime().plusSeconds(life));

                log.info("==========[Rank update {}]", title);
                writMapper.updateTitle(title);
            }
        }

        log.info("==========[Writ rank reward end]");
    }

}
