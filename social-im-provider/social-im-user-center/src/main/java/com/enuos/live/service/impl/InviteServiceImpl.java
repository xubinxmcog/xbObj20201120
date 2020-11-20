package com.enuos.live.service.impl;

import com.enuos.live.constant.BigDecimalEnum;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.OrderFeign;
import com.enuos.live.manager.CurrencyEnum;
import com.enuos.live.mapper.*;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.InviteService;
import com.enuos.live.service.RewardService;
import com.enuos.live.task.Key;
import com.enuos.live.task.TaskEnum;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 分享邀请
 * @Author wangyingjie
 * @Date 2020/8/4
 * @Modified
 */
@Slf4j
@Service
public class InviteServiceImpl extends BaseService implements InviteService {

    private static final Integer[] URL_TYPE = {5, 6};

    private static final String DESCRIPTION = "奖励提现";

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private InviteMapper inviteMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private WeChatMapper weChatMapper;

    @Autowired
    private AgreementMapper agreementMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * @Description: 受邀者完成分享任务处理
     * @Param: [params]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/8/6
     */
    @Override
    @Transactional
    public void newbieTask(Long userId, String templateCode) {
        log.info("TASK[TN] Invite newbieTask begin, params[userId:{}, templateCode:{}]", userId, templateCode);

        InviteUser inviteUser = inviteMapper.getInviterUser(userId);
        if (inviteUser == null) {
            log.info("TASK[TN] Invite newbieTask inviteUser is null");
            return;
        }

        Long inviterId = inviteUser.userId;
        LocalDateTime currentTime = DateUtils.getCurrentDateTimeOfPattern();
        List<Map<String, Object>> rewardList;
        Map<String, Object> rmbReward = null;
        StringBuilder code = new StringBuilder(TaskEnum.I01.CODE);
        TaskParam taskParam;
        if (TemplateEnum.L01.CODE.equals(templateCode)) {
            // 受邀人首登
            if (inviteUser.getToUserId() != null) {
                // 非首登
                log.info("TASK[TN] Invite newbieTask not is first login");
                return;
            }

            // 设备号是否存在其他人使用
            if (deviceMapper.getCount(userId) > 1) {
                log.info("TASK[TN] Invite newbieTask not is first login");
                return;
            }

            inviteUser.setToUserId(userId);
            inviteUser.setLoginTime(currentTime);
            inviteMapper.updateInviterUser(inviteUser);

            // 是否为首位受邀人
            taskParam = new TaskParam(inviterId, TaskEnum.I01.CODE, templateCode);
            Integer isExists = taskMapper.isExistsRecord(taskParam);
            if (isExists == null) {
                // 获取奖励并发放
                rewardList = taskMapper.getReward(taskParam);
                if (CollectionUtils.isNotEmpty(rewardList)) {
                    rewardService.handler(userId, rewardList.stream().filter(r -> !CurrencyEnum.RMB.CODE.equals(MapUtils.getString(r, "rewardCode"))).collect(Collectors.toList()));
                }
                rmbReward = rewardList.stream().filter(r -> CurrencyEnum.RMB.CODE.equals(MapUtils.getString(r, "rewardCode"))).findFirst().orElse(null);
                code = code.append(".").append(templateCode);
                // 保存记录
                taskMapper.saveRecord(taskParam);
            }
        } else if (TemplateEnum.G01.CODE.equals(templateCode)) {
            // 受邀人玩游戏
            if (inviteUser.getToUserId() == null) {
                log.info("TASK[TN] Invite newbieTask toUser not is newbie");
                return;
            }

            if (currentTime.isAfter(inviteUser.getLoginTime().plusDays(7))) {
                log.info("TASK[TN] Invite newbieTask The current time has expired");
                return;
            }

            // 今日是否达标
            String dayKey = Key.getTaskDay(userId);
            int second = (int) redisUtils.getHash(dayKey, templateCode);
            int minute = second / 60;
            if (minute > 10) {
                // 今日是否完成
                taskParam = new TaskParam(userId, TaskEnum.I01.CODE, templateCode, currentTime.toLocalDate());
                Integer isExists = taskMapper.isExistsRecord(taskParam);
                if (isExists != null) {
                    log.info("TASK[TN] Invite newbieTask The task has done");
                    return;
                }

                Map<String, Object> taskFollow = taskMapper.getFollow(taskParam);
                int progress = 0;
                if (MapUtils.isEmpty(taskFollow)) {
                    progress = 1;
                    taskParam.setProgress(1);
                    taskMapper.saveFollow(taskParam);
                } else if (MapUtils.getIntValue(taskFollow, "progress") < 3) {
                    progress = MapUtils.getIntValue(taskFollow, "progress") + 1;
                    taskMapper.updateFollow(MapUtils.getInteger(taskFollow, "id"), 1);
                }

                if (progress > 0) {
                    rewardList = taskMapper.getReward(taskParam);
                    if (CollectionUtils.isNotEmpty(rewardList)) {
                        rmbReward = rewardList.get(0);
                        code = code.append(".").append(templateCode).append(".").append(progress).append(".").append(progress);
                    }

                    taskParam.setCategory(MapUtils.getInteger(rmbReward, "category"));
                    taskParam.setSuffix(MapUtils.getInteger(rmbReward, "suffix"));
                    taskMapper.saveRecord(taskParam);
                }
            }
        }

        // RMB奖励审核单据增加收益
        if (MapUtils.isNotEmpty(rmbReward)) {
            BigDecimal addMoney = new BigDecimal(MapUtils.getString(rmbReward, "number"));
            inviteMapper.saveAuditMoney(code.toString(), inviterId, userId, addMoney);
            // 更新总收益
            Invite invite = inviteMapper.getInvite(inviterId);
            invite.setSumMoney(BigDecimalUtil.nAdd(invite.getSumMoney(), addMoney));
            inviteMapper.updateInvite(invite);
        }

        log.info("TASK[TN] Invite newbieTask end");
    }

    /**
     * @Description: 接受邀请
     * @Param: [inviteUser]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/4
     */
    @Override
    @Transactional
    public Result accept(InviteUser inviteUser) {
        String account = inviteUser.getToUserAccount();
        String code = inviteUser.getCode();

        // 验证码是否一致
        String redisKey = RedisKey.KEY_SMS_CODE.concat(account);
        if (!redisUtils.hasKey(redisKey)) {
            return Result.error(ErrorCode.SMS_CODE_EXPIRED);
        }
        String redisCode = redisUtils.get(redisKey).toString();
        if (!Objects.equals(code, redisCode)) {
            return Result.error(ErrorCode.SMS_CODE_DIFFERENCE);
        }

        // 是否合法
        if (!StringUtils.isPhone(account)) {
            return Result.error(ErrorCode.ACCOUNT_ILLEGAL);
        }

        // 是否存在
        if (inviteMapper.isExistsAccount(account) != null) {
            return Result.error(ErrorCode.ACCOUNT_EXISTS);
        }

        // 是否被他人邀请
        if (inviteMapper.isExistsInviterUser(account) != null) {
            return Result.error(ErrorCode.ACCOUNT_EXISTS_INVITE);
        }

        // 创建邀请者关系
        if (inviteMapper.isExistsInviter(inviteUser.userId) == null) {
            inviteMapper.saveInviter(inviteUser.userId);
        }

        // 创建邀请者与受邀者关系
        inviteMapper.saveInviterUser(inviteUser);

        return Result.success();
    }

    /**
     * @Description: 我的奖励
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/5
     */
    @Override
    public Result myReward(Long userId) {
        if (userId == null) {
            return Result.empty();
        }

        Invite invite = Optional.ofNullable(inviteMapper.getInviteInfo(userId)).orElse(new Invite());

        List<InviteMoney> inviteAuditVOList = inviteMapper.getAuditMoney(userId);
        if (CollectionUtils.isNotEmpty(inviteAuditVOList)) {
            // 分组求和，待审核合计&已审核合计
            Map<Integer, BigDecimal> map = inviteAuditVOList.stream().collect(Collectors.groupingBy(InviteMoney::getAuditStatus, CollectorsUtils.summingBigDecimal(InviteMoney::getAuditMoney)));
            BigDecimal ableMoney = map.containsKey(1) ? map.get(1) : BigDecimal.ZERO;
            // 通过审核的可提现，剩余可提现=总可提现-已提现
            invite.setAbleMoney(BigDecimalUtil.nSub(ableMoney, invite.getHaveMoney()));
            invite.setAuditMoney(map.containsKey(0) ? map.get(0) : BigDecimal.ZERO);
        }

        // 初始化bigDecimal
        initPropertyNumber(invite);

        // 设置是否绑定微信
        WeChat weChat = weChatMapper.getWeChat(userId);
        if (weChat == null) {
            invite.setIsBindWeChatPay(0);
        } else {
            invite.setIsBindWeChatPay(1);
            invite.setWeChatNickName(weChat.getWeChatNickName());
        }

        // 设置Url
        Map<Integer, String> urlMap = agreementMapper.getUrl(URL_TYPE).stream().collect(Collectors.toMap(k -> MapUtils.getInteger(k, "type"), v -> MapUtils.getString(v, "content"), (k1, k2) -> k1));
        invite.setBackgroundUrl(Optional.ofNullable(urlMap.get(5)).orElse(""));
        invite.setRuleUrl(Optional.ofNullable(urlMap.get(6)).orElse(""));

        // 设置提现公告
        invite.setNoticeList(inviteMapper.getRecordListByLimit(10));

        return Result.success(invite);
    }

    /**
     * @Description: 提现
     * @Param: [inviteMoney]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/5
     */
    @Override
    @Transactional
    public Result toGet(InviteMoney inviteMoney) {
        Long userId = inviteMoney.userId;
        WeChat weChat = weChatMapper.getWeChat(userId);
        // 微信是否绑定
        if(weChat == null) {
            return Result.error(ErrorCode.ACCOUNT_UNBINDING_WECHAT);
        }

        // 微信是否实名（暂无实现）

        BigDecimal getMoney = inviteMoney.getGetMoney();
        // 校验金额是否合法
        if (getMoney.compareTo(BigDecimal.ZERO) == 0 || getMoney.compareTo(BigDecimalEnum.RMB1.getMoney()) == -1) {
            return Result.error(ErrorCode.MONEY_ILLEGAL);
        }

        // 校验可提现金额
        Invite invite = inviteMapper.getAbleMoney(userId);
        BigDecimal ableMoney = invite == null ? BigDecimal.ZERO : BigDecimalUtil.nSub(invite.getAuditMoney(), invite.getHaveMoney());
        if (getMoney.compareTo(ableMoney) == 1) {
            return Result.error(ErrorCode.MONEY_NOT_ENOUGT);
        }

        // 校验今日已提现金额
        BigDecimal todayGetMoney = inviteMapper.getTodayGetMoney(userId, LocalDate.now());
        if (todayGetMoney != null && getMoney.compareTo(BigDecimalUtil.nSub(BigDecimalEnum.RMB30.getMoney(), todayGetMoney)) == 1) {
            return Result.error(ErrorCode.MONEY_TODAY_NOT_ENOUGT);
        }

        String orderId = RandomUtil.getRandom();
        inviteMoney.setOrderId(orderId);
        inviteMoney.setDescription(DESCRIPTION);
        inviteMapper.saveMoneyGet(inviteMoney);

        // 调用支付（自动支付）
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(userId));
        params.put("openid", weChat.getOpenId());
        params.put("tradeno", orderId);
        params.put("amount", getMoney.toString());
        params.put("desc", DESCRIPTION);

        threadPoolTaskExecutor.submit(() -> orderFeign.businessPay(params));

        invite = inviteMapper.getHaveMoney(orderId);
        invite.setHaveMoney(BigDecimalUtil.nAdd(invite.getHaveMoney(), invite.getGetMoney()));
        // 添加已获取
        inviteMapper.updateInvite(invite);

        return Result.success(inviteMoney);
    }

    /**
     * @Description: 提现记录
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/6
     */
    @Override
    public Result record(Long userId) {
        if (userId == null) {
            return Result.empty();
        }

        List<InviteMoney> recordList = inviteMapper.getRecordList(userId);

        return Result.success(recordList);
    }
}
