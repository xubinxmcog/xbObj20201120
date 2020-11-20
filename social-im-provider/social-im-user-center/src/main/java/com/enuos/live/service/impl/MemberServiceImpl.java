package com.enuos.live.service.impl;

import com.enuos.live.constants.TaskConstants;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.NettyFeign;
import com.enuos.live.feign.OrderFeign;
import com.enuos.live.mapper.MemberMapper;
import com.enuos.live.pojo.Member;
import com.enuos.live.result.Result;
import com.enuos.live.service.AchievementService;
import com.enuos.live.service.CommonService;
import com.enuos.live.service.MemberService;
import com.enuos.live.service.RewardService;
import com.enuos.live.utils.BeanUtils;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description 会员中心
 * @Author wangyingjie
 * @Date 2020/6/29
 * @Modified
 */
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    /**
     * 会员装饰
     */
    private static Map<Integer, List<Map<String, Object>>> MEMBER_DECORATION;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private NettyFeign nettyFeign;

    @Autowired
    private CommonService commonService;

    /**
     * @Description: 处理会员装饰
     * @Param: [userId, vip]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/31
     */
    @Override
    public void decorationHandler(Long userId, Integer vip) {
        if (MEMBER_DECORATION.containsKey(vip)) {
            orderFeign.addBackpack(new HashMap<String, Object>() {
                {
                    put("userId", userId);
                    put("list", MEMBER_DECORATION.get(vip));
                }
            });
        }
    }

    /**
     * @Description: 添加成长值
     * @Param: [userId, growth]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/20
     */
    @Override
    @Transactional
    public Result addGrowth(Long userId, Integer growth) {
        if (growth == null || growth == 0) {
            return Result.empty();
        }

        log.info("==========[AddGrowth begin, userId is {}, growth is {}]==========", userId, growth);
        // 判定会员是否过期
        Member member = memberMapper.getMember(userId);
        if (member == null) {
            log.error("Member is null");
            return Result.error(ErrorCode.NO_DATA);
        }

        if (!member.getExpirationTime().isAfter(LocalDateTime.now())) {
            log.info("Member is expired");
            return Result.error(ErrorCode.MEMBER_EXPIRED);
        }

        // 获取会员配置
        List<Map<String, Object>> configList = memberMapper.getVipConfig();
        if (CollectionUtils.isEmpty(configList)) {
            log.error("Vip config is empty");
            return Result.error(ErrorCode.NO_DATA);
        }
        Map<Integer, Integer> settings = configList.stream().collect(Collectors.toMap(k -> MapUtils.getInteger(k, "code"), v -> MapUtils.getInteger(v, "threshold"), (k1, k2) -> k1));

        int maxVip = settings.get(0);
        int vip = member.getVip();
        int originalVipGrade = member.getVip();

        int maxGrowth = settings.get(maxVip);
        int sumGrowth = member.getGrowth() + growth > maxGrowth ? maxGrowth : (member.getGrowth() + growth);

        while (sumGrowth >= settings.get(vip) && vip < maxVip) {
            // 大于当前阈值升级
            vip++;
            // 小于下一级阈值则还是当前等级
            if (sumGrowth < settings.get(vip)) {
                vip--;
                break;
            }
            // 会员等级成就
            achievementHandlers(userId);
            // 会员升级奖励会员专属装饰
            if (MEMBER_DECORATION.containsKey(vip)) {
                Map<String, Object> params = new HashMap<>();
                params.put("userId", userId);
                params.put("list", MEMBER_DECORATION.get(vip));
                orderFeign.addBackpack(params);
            }
        }

        member.setVip(vip);
        member.setGrowth(sumGrowth);

        memberMapper.updateMember(member);

        vipGradeNotice(member, originalVipGrade);
        log.info("==========[AddGrowth end]==========");
        return Result.success();
    }

    // 会员等级达到v4及以上全服通告
    @Async
    void vipGradeNotice(Member member, int originalVipGrade) {
        Integer vip = member.getVip();
        log.info("会员升级全服通告,当前vip等级=[{}],原等级=[{}]", vip, originalVipGrade);
        if (vip > 3 && vip > originalVipGrade) {
            Map<String, Object> userBase = commonService.getUserBase(member.getUserId(), "nickName", "thumbIconUrl");

            Map<String, Object> map = new HashMap<>();
            map.put("userId", member.getUserId());
            map.put("nickName", MapUtils.getString(userBase, "nickName"));
            map.put("thumbIconURL", MapUtils.getString(userBase, "thumbIconUrl"));
            map.put("vip", vip);
            Result result = nettyFeign.vipGradeNotice(map);
            log.info("会员等级达到v4及以上全服通告结果=[{}]", result.getCode());
        }
    }

    /**
     * @Description: 会员中心
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    @Override
    public Result center(Long userId) {
        if (userId == null || userId <= 0) {
            return Result.empty();
        }

        Member member = memberMapper.getMember(userId);
        if (member == null) {
            return Result.error(ErrorCode.NO_DATA);
        }

        int vip = member.getVip();
        LocalDateTime expirationTime = member.getExpirationTime();

        List<Map<String, Object>> list = memberMapper.getVipConfig();
        Map<Integer, Map<String, Object>> settings = list.stream().collect(Collectors.toMap(k -> MapUtils.getInteger(k, "code"), Function.identity(), (k1, k2) -> k1));

        int vipMax = MapUtils.getIntValue(settings.get(0), "threshold");
        int vipLine = vip + 1 > vipMax ? vipMax : vip + 1;

        member.setVipLine(vipLine);
        member.setGrowthLine(MapUtils.getInteger(settings.get(vipLine), "threshold"));
        member.setExpiration(getExpiration(vip, expirationTime));
        member.setVipIconUrl(MapUtils.getString(settings.get(vip), "url"));

        member.setInterestList(memberMapper.getMemberInterestList());
        member.setShipUrl(memberMapper.ship());
        member.setIsMember(isMember(userId));

        return Result.success(member);
    }

    /**
     * @Description: 充值套餐
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    @Override
    public Result rechargePackage() {
        return Result.success(memberMapper.getRechargePackage());
    }

    /**
     * @Description: 充值结果
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    @Override
    @Transactional
    public Result rechargeResult(Map<String, Object> params) {
        Long userId = MapUtils.getLong(params, "userId");
        String productId = MapUtils.getString(params, "productId");

        // 获取充值套餐
        Map<String, Object> map = memberMapper.getRechargePackageByCode(productId);
        if (MapUtils.isEmpty(map)) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "Can not get member package");
        }

        String durationUnit = MapUtils.getString(map, "durationUnit");
        int duration = MapUtils.getIntValue(map, "duration");

        if ("M".equals(durationUnit)) {
            // 获取用户会员信息
            Member member = memberMapper.getMember(userId);
            if (member == null) {
                return Result.error(ErrorCode.EXCEPTION_CODE, "Can not get member");
            }

            int vip = member.getVip();
            LocalDateTime expirationTime = member.getExpirationTime();
            LocalDateTime currentTime = LocalDateTime.now();

            // 当会员时限=当前时限，考虑延后性，为非会员
            expirationTime = expirationTime.isAfter(currentTime) ? expirationTime : currentTime;

            member.setVip(vip == 0 ? 1 : null);
            // 31天
            member.setExpirationTime(expirationTime.plusDays(duration * 31));
            memberMapper.updateExpirationTime(member);

            // 首次充值vip1奖励会员专属装饰&成就达成
            if (vip == 0) {
                rewardService.handler(userId, MEMBER_DECORATION.get(1));
                achievementHandlers(userId);
            }
        }

        return Result.success();
    }

    /**
     * @Description: 是否会员
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    @Override
    public Integer isMember(Long userId) {
        Member member = memberMapper.getSimpleMember(userId);
        if (Objects.isNull(member)) {
            return null;
        }
        return member.getVip() > 0 ? member.getExpirationTime().isAfter(DateUtils.getCurrentDateTime()) ? 1 : -1 : 0;
    }

    /**
     * @Description: yyyy年MM月dd日
     * @Param: [vip, expirationTime]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    private String getExpiration(int vip, LocalDateTime expirationTime) {
        return vip == 0 ? "开通会员即可享受海量特权" : expirationTime.isBefore(LocalDateTime.now()) ? "会员过期" : new StringBuffer("会员期限至").append(expirationTime.getYear()).append("年").append(expirationTime.getMonthValue()).append("月").append(expirationTime.getDayOfMonth()).append("日").toString();
    }

    /**
     * @Description: 会员升级成就处理
     * @Param: [userId]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/20
     */
    private void achievementHandlers(Long userId) {
        achievementService.handlers(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("list", BeanUtils.deepCopyByJson(TaskConstants.MEMBERLIST, ArrayList.class));
            }
        });
    }

    /**
     * @Description: 会员装饰奖励
     * @Param: []
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/31
     */
    @PostConstruct
    private void initMemberDecoration() {
        List<Map<String, Object>> list = memberMapper.getMemberDecoration();
        if (CollectionUtils.isEmpty(list)) {
            log.error("==========[initMemberDecoration error]==========");
            return;
        }

        MEMBER_DECORATION = list.stream().collect(Collectors.groupingBy(m -> MapUtils.getInteger(m, "vip")));
    }
}
