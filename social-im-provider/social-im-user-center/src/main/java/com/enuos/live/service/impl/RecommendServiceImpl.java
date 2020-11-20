package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.RecommendMapper;
import com.enuos.live.pojo.Recommend;
import com.enuos.live.pojo.UserFriends;
import com.enuos.live.result.Result;
import com.enuos.live.service.RecommendService;
import com.enuos.live.utils.SortUtils;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 推荐用户
 * @Author wangyingjie
 * @Date 17:36 2020/4/16
 * @Modified
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    private static final Integer LIMIT = 5;

    @Autowired
    private RecommendMapper recommendMapper;

    /**
     * 获取推荐的用户 根据socre评分来获取推荐用户，评分越高，相似度越高，推荐度越高
     * score(A,B) = (∑1/√(Neghbor(k))) / union
     * Neghbor(k)为AB共同好友N的好友数
     * union = A ∪ B(AB好友并集)
     * @param userId
     * @return
     */
    @Override
    public Result getUserByScore(Long userId) {
        if (Objects.isNull(userId)) {
            return Result.empty();
        }

        // 以下用A标识当前用户
        // 获取A的直接小伙伴和潜在小伙伴
        List<UserFriends> list = recommendMapper.getUser(userId);
        if (CollectionUtils.isEmpty(list)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        // 获取A的直接小伙伴
        List<Long> friendIdList = list.stream().map(UserFriends::getFriendId).collect(Collectors.toList());

        // 好友关系矩阵
        Map<Long, List<Long>> map = new HashMap<Long, List<Long>>();
        getFriendIdMap(map, list);
        if (MapUtils.isEmpty(map)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        List<Map<String, Object>> userScoreList = new ArrayList<Map<String, Object>>();

        map.forEach((k, v) -> {
            // B非A && B存在好友 && B与A无直接关系
            if (!Objects.equals(k, userId) && CollectionUtils.isNotEmpty(v) && !v.contains(userId)) {
                // 筛选B与A共同好友
                List<Long> intersectionIdList = v.stream().filter(id -> friendIdList.contains(id)).collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(intersectionIdList)) {
                    // AB交集
                    Integer intersection = intersectionIdList.size();
                    // AB并集 jaccardIndex = interseciton / union
                    Integer union = friendIdList.size() + v.size() - intersectionIdList.size();

                    // B的加权得分
                    Double score = 0.0;
                    for (Long i : intersectionIdList) {
                        score += 1 / Math.sqrt(map.get(i).size());
                    }

                    Map<String, Object> jaccardMap = new HashMap<String, Object>();
                    jaccardMap.put("userId", k);
                    // jaccardIndex 杰卡德相似系数
                    jaccardMap.put("jaccardIndex", (float) (intersection / union));
                    jaccardMap.put("score", score / union);

                    userScoreList.add(jaccardMap);
                }
            }
        });

        List<Map<String, Object>> resultList = new ArrayList<>();
        // 获取已经拉黑A或账号被冻结的小朋友
        if (CollectionUtils.isNotEmpty(userScoreList)) {
            List<Long> relationList = recommendMapper.getBlackList(userId, userScoreList);
            // 过滤掉不友好的小朋友
            resultList = userScoreList.stream().filter(sMap -> !relationList.contains(sMap.get("userId"))).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(resultList)) {
            return Result.success(resultList);
        }

        // 排个队
        SortUtils.sort(resultList, false, "score");
        if (resultList.size() > LIMIT) {
            resultList.subList(LIMIT, resultList.size()).clear();
        }

        // 得到最终推荐，去拿头像昵称
        List<Map<String, Object>> recommendUserList = recommendMapper.getUserList(resultList);

        return Result.success(recommendUserList);
    }

    /**
     * @Description: 根据等级推荐用户
     * @Param: [recommend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result getUserByLevel(Recommend recommend) {
        if (Objects.isNull(recommend)) {
            return Result.empty();
        }

        PageHelper.startPage(recommend.getPageNum(), recommend.getPageSize());
        List<Recommend> userList = recommendMapper.getUserByLevel(recommend);

        return Result.success(new PageInfo<>(userList));
    }


    /** [PRIVATE] */

    /**
     * @Description: 整理参数（递归）
     * @Param: [map, friendList]
     * @Return: java.util.Map<java.lang.Long,java.util.List<java.lang.Long>>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    private Map<Long, List<Long>> getFriendIdMap(Map<Long, List<Long>> map, List<UserFriends> friendList) {
        friendList.forEach(friend -> {
            if (CollectionUtils.isNotEmpty(friend.getFriendList())) {
                map.put(friend.getFriendId(), friend.getFriendList().stream().map(UserFriends::getFriendId).collect(Collectors.toList()));
                getFriendIdMap(map, friend.getFriendList());
            }
        });
        return map;
    }

}
