package com.enuos.live.service.impl;

import com.enuos.live.constants.ProductConstants;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.BadgeMapper;
import com.enuos.live.mapper.UserMapper;
import com.enuos.live.pojo.Badge;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;
import com.enuos.live.service.BadgeService;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 用户徽章
 * @Author wangyingjie
 * @Date 2020/7/16
 * @Modified
 */
@Slf4j
@Service
public class BadgeServiceImpl implements BadgeService {

    @Autowired
    private BadgeMapper badgeMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * @Description: 获取主页徽章
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    @Override
    public Result wearBadgeList(User user) {
        if (Objects.isNull(user) || Objects.isNull(user.getToUserId())) {
            return Result.empty();
        }

        List<Badge> list = badgeMapper.getWearBadgeList(user.getToUserId());
        if (CollectionUtils.isNotEmpty(list)) {
            Integer sex = userMapper.getSex(user.getToUserId());
            for (Badge b : list) {
                if (ArrayUtils.contains(ProductConstants.BADGE_BY_SEX_CODE, b.getCode())) {
                    b.setIconUrl(splitBySex(sex, b.getIconUrl()));
                }
            }
        }

        return Result.success(list);
    }

    /**
     * @Description: 获得的徽章
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/18
     */
    @Override
    public Result num(Badge badge) {
        if (Objects.isNull(badge)) {
            return Result.empty();
        }

        Map<String, Object> numMap = badgeMapper.getNum(badge.userId);

        return Result.success(numMap);
    }

    /**
     * @Description: 用户徽章墙
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @Override
    public Result list(Badge badge) {
        if (Objects.isNull(badge)) {
            return Result.empty();
        }

        Long userId = badge.userId;

        PageHelper.startPage(badge.pageNum, badge.pageSize);

        List<Integer> typeList = badgeMapper.getType();
        PageInfo pageInfo = new PageInfo<>(typeList);

        List<Badge> badgeList = badgeMapper.getBadgeList(userId, typeList);

        Integer sex = userMapper.getSex(userId);

        for (Badge b : badgeList) {
            if (ArrayUtils.contains(ProductConstants.BADGE_BY_SEX_CODE, b.getCode())) {
                b.setTitle(splitBySex(sex, b.getTitle()));
                b.setIconUrl(splitBySex(sex, b.getIconUrl()));
            }
        }

        List<List<Badge>> pageList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(badgeList)) {
            TreeMap<Integer, List<Badge>> treeMap = badgeList.stream().collect(Collectors.groupingBy(Badge::getType, TreeMap::new, Collectors.toList()));
            pageList.addAll(treeMap.values());
        }

        pageInfo.setList(pageList);

        return Result.success(pageInfo);
    }

    /**
     * @Description: 佩戴
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @Override
    public Result wear(Badge badge) {
        if (Objects.isNull(badge) || Objects.isNull(badge.userId) || StringUtils.isBlank(badge.getCode())) {
            return Result.empty();
        }

        Long userId = badge.userId;
        String code = badge.getCode();

        Integer wear = badgeMapper.getWear(userId, code);

        if (wear == null) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "the wear is null");
        } else if (wear == 0 && badgeMapper.getWearNum(userId) >= 5) {
            // 至多佩戴5个
            return Result.error(ErrorCode.BADGE_WEAR_MAX);
        }

        // 取反
        wear = wear == 1 ? 0 : 1;

        int result = badgeMapper.updateWear(userId, code, wear);

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * ==========[内部调用]==========
     */

    /**
     * @Description: 批量保存
     * @Param: [userId, badgeList]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    @Transactional
    public Result batchSave(Long userId, List<Map<String, Object>> badgeList) {
        // 获取已经存在的徽章
        List<String> badgeCodeList = badgeMapper.getBadgeCode(userId, badgeList);
        // 过滤
        if (CollectionUtils.isNotEmpty(badgeCodeList)) {
            badgeList = badgeList.stream().filter(badge -> !badgeCodeList.contains(MapUtils.getString(badge, "rewardCode"))).collect(Collectors.toList());
        }

        int result = 0;
        if (CollectionUtils.isNotEmpty(badgeList)) {
            result = badgeMapper.batchSave(userId, badgeList);
        }

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @Description: 拆分
     * @Param: [sex, item]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/9/18
     */
    private String splitBySex(Integer sex, String item) {
        return sex == 1 ? StringUtils.substringBeforeLast(item, ",") : StringUtils.substringAfterLast(item, ",");
    }
}
