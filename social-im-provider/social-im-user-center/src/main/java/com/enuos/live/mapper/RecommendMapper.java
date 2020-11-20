package com.enuos.live.mapper;

import com.enuos.live.pojo.Recommend;
import com.enuos.live.pojo.UserFriends;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 推荐DAO
 * @Author wangyingjie
 * @Date 17:35 2020/4/16
 * @Modified
 */
public interface RecommendMapper {

    /**
     * 获取用户二度好友
     * @param userId
     * @return
     */
    List<UserFriends> getUser(Long userId);

    /**
     * 查看是否在推荐好友的黑名单中
     * @param userId
     * @param recommendList
     * @return
     */
    List<Long> getBlackList(@Param("userId") Long userId, @Param("recommendList") List<Map<String, Object>> recommendList);

    /**
     * 获取用户信息
     * @param recommendList
     * @return
     */
    List<Map<String, Object>> getUserList(List<Map<String, Object>> recommendList);

    /**
     * 推荐用户[level]
     * @param recommend
     * @return
     */
    List<Recommend> getUserByLevel(Recommend recommend);

}
