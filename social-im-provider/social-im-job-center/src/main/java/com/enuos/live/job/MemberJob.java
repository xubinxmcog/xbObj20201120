package com.enuos.live.job;

import com.enuos.live.constants.TaskConstants;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.MemberMapper;
import com.enuos.live.pojo.Member;
import com.enuos.live.utils.BeanUtils;
import com.enuos.live.utils.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 会员权益
 * @Author wangyingjie
 * @Date 2020/7/1
 * @Modified
 */
@Slf4j
@Component
public class MemberJob {

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private MemberMapper memberMapper;

    /**
     * @Description: 1：会员每日免费扭蛋券； 2：会员期间，每天自动增加10点成长值；3：过期会员，每天自动减少15点成长值
     * @Param: []
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/2
     */
    // @Scheduled(cron = "*/10 * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void memberInterest() {
        LocalDateTime currentTime = LocalDateTime.now();

        log.info("=========[MemberJob memberInterest begin, time is {}]==========", currentTime.toString());

        // 扭蛋设置
        List<Map<String, Object>> gsList = memberMapper.getGashaponInterest();
        if (CollectionUtils.isEmpty(gsList)) {
            log.info("Gashapon interest settings is [{}]", gsList);
            return;
        }
        Map<Integer, Integer> gSettings = gsList.stream().collect(Collectors.toMap(k -> MapUtils.getInteger(k, "vip"), v -> MapUtils.getInteger(v, "exclusiveNum"), (k1, k2) -> k1));

        // 会员设置
        List<Map<String, Object>> vslist = memberMapper.getVipConfig();
        if (CollectionUtils.isEmpty(vslist)) {
            log.info("Vip growth settings is [{}]", vslist);
            return;
        }
        Map<Integer, Integer> vSettings = vslist.stream().collect(Collectors.toMap(k -> MapUtils.getInteger(k, "code"), v -> MapUtils.getInteger(v, "threshold"), (k1, k2) -> k1));

        // 获取会员与过期会员
        List<Member> memberList = memberMapper.getMemberList();
        if (CollectionUtils.isEmpty(memberList)) {
            log.info("Members is [{}]", memberList);
            return;
        }

        memberList.forEach(member -> {
            // 会员时限=当前时限，考虑延后性，为非会员
            if (member.getExpirationTime().isAfter(currentTime)) {
                // 会员
                member.setGashapon(BigDecimalUtil.nAdd(member.getGashapon(), gSettings.get(member.getVip())));
                addGrowth(member, vSettings);
            } else {
                // 过期会员
                subGrowth(member, vSettings);
            }
        });

        // update
        memberMapper.batchUpdateMember(memberList);

        log.info("=========[MemberJob memberInterest end]==========");
    }

    /**
     * @Description: 会员加成长值
     * @Param: [member, settings]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/2
     */
    private void addGrowth(Member member, Map<Integer, Integer> settings) {
        int max = settings.get(0);

        int vip = member.getVip();
        int vipNext = vip + 1 > max ? max : vip + 1;

        int growth = member.getGrowth();
        int growthLine = settings.get(vipNext);
        int growthMax = settings.get(max);

        growth = growth + 10 > growthMax ? growthMax : (growth + 10);

        if (growth >= growthLine) {
            member.setVip(vipNext);
            achievementHandlers(member.getUserId());

            userFeign.decorationHandler(member.getUserId(), vipNext);
        }

        member.setGrowth(growth);
    }

    /**
     * @Description: 过期会员减成长值
     * @Param: [member, settings]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/2
     */
    private void subGrowth(Member member, Map<Integer, Integer> settings) {
        if (member.getGrowth() == 0) {
            return;
        }

        int min = 1;

        int vip = member.getVip();
        int vipNext = vip - 1 < min ? min : vip - 1;

        int growth = member.getGrowth();
        int growthLine = settings.get(vip);

        growth = growth <= 15 ? 0 : growth - 15;

        member.setVip(growth < growthLine ? vipNext : vip);
        member.setGrowth(growth);
    }

    /**
     * @Description: 会员升级成就处理
     * @Param: [userId]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/16
     */
    private void achievementHandlers(Long userId) {
        userFeign.achievementHandlers(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("list", BeanUtils.deepCopyByJson(TaskConstants.MEMBERLIST, ArrayList.class));
            }
        });
    }

}