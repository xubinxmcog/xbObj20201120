package com.enuos.live.service.impl;

import com.enuos.live.constants.ProductConstants;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.mapper.LevelMapper;
import com.enuos.live.mapper.RewardMapper;
import com.enuos.live.mapper.TaskRewardRecordMapper;
import com.enuos.live.mapper.UserMapper;
import com.enuos.live.pojo.Level;
import com.enuos.live.result.Result;
import com.enuos.live.service.LevelService;
import com.enuos.live.service.RewardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Description 等级中心
 * @Author wangyingjie
 * @Date 2020/7/21
 * @Modified
 */
@Slf4j
@Service
public class LevelServiceImpl implements LevelService {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private LevelMapper levelMapper;

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private TaskRewardRecordMapper taskRewardRecordMapper;

    @Autowired
    private UserMapper userMapper;


    /**
     * @Description: 等级条
     * @Param: [levelPO]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/21
     */
    @Override
    public Result bar(Level level) {
        if (level == null) {
            return Result.empty();
        }

        Long userId = level.userId;

        // 获取当前等级
        Integer lv = levelMapper.getLevel(userId);
        if (lv == null) {
            log.error("No level, params is [userId:{}]", userId);
            return Result.error(ErrorCode.NO_DATA);
        }

        // 获取等级阈值
        List<Map<String, Object>> list = levelMapper.getLevelThreshold(userId);
        if (CollectionUtils.isEmpty(list)) {
            log.error("No threshold, params is [userId:{}]", userId);
            return Result.error(ErrorCode.NO_DATA);
        }

        // 获取奖励
        List<String> recordList = taskRewardRecordMapper.getRecordByPrefix(userId, TaskEnum.LVT0001.getCode());

        int tlv;
        for (Map<String, Object> map : list) {
            tlv = MapUtils.getIntValue(map, "level");
            String code = TaskEnum.LVT0001.getCode().concat("_").concat(String.valueOf(tlv));
            // isGot 是否获取奖励 [-1 未获得 0 可获得 1 已获得] 1级奖励已领取（其实啥都没有初始即1级）
            map.put("isGot", tlv == 1 ? 1 : lv >= tlv ? recordList.contains(code) ? 1 : 0 : -1);
            // 当前用户已达到的等级，经验值为阈值；没达到的等级，经验值为0；
            map.put("experience", lv > tlv ? map.get("threshold") : lv < tlv ? 0 : map.get("experience"));
        }

        return Result.success(list);
    }

    /**
     * @Description: 等级奖励
     * @Param: [level]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/21
     */
    @Override
    public Result reward(Level level) {
        // 应用场景账号初始化为1级，该等级没有奖励
        if (level.getLevel() == 1) {
            return Result.success();
        }

        List<Map<String, Object>> list = rewardMapper.getRewardByCode(TaskEnum.LVT0001.getCode(), level.getLevel());
        if (CollectionUtils.isEmpty(list)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        int sex = userMapper.getSex(level.userId);
        String item;
        for (Map<String, Object> map : list) {
            if (ArrayUtils.contains(ProductConstants.BADGE_BY_SEX_CODE, MapUtils.getString(map, "rewardCode"))) {
                item = MapUtils.getString(map, "rewardName");
                map.put("rewardName", sex == 1 ? StringUtils.substringBeforeLast(item, ",") : StringUtils.substringAfterLast(item, ","));
                item = MapUtils.getString(map, "url");
                map.put("url", sex == 1 ? StringUtils.substringBeforeLast(item, ",") : StringUtils.substringAfterLast(item, ","));
            }
        }

        return Result.success(list);
    }

    /**
     * @Description: 领奖
     * @Param: [levelPO]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/21
     */
    @Override
    @Transactional
    public Result toGet(Level level) {
        Long userId = level.userId;
        Integer lv = level.getLevel();

        String code = TaskEnum.LVT0001.getCode().concat("_").concat(String.valueOf(lv));
        // 是否领取
        if (taskRewardRecordMapper.isExists(userId, code) != null) {
            return Result.error(ErrorCode.REWARD_IS_GOT);
        }

        // 是否可领
        Integer tlv = levelMapper.getLevel(userId);
        if (tlv < lv) {
            return Result.error(ErrorCode.REWARD_NO_PERMISSION);
        }

        List<Map<String, Object>> list = rewardMapper.getRewardByCode(TaskEnum.LVT0001.getCode(), lv);
        if (CollectionUtils.isEmpty(list)) {
            log.error("Can not get reward param[userId:{}, taskCode:{}, suffix:{}]", userId, TaskEnum.LVT0001.getCode(), lv);
            return Result.error(ErrorCode.REWARD_NOT_EXISTS);
        }

        // 奖励获取
        rewardService.handler(userId, list);
        // 记录
        taskRewardRecordMapper.save(userId, code);

        return Result.success();
    }

}