package com.enuos.live.mapper;

import com.enuos.live.pojo.GiftExtraReward;
import com.enuos.live.pojo.UserTitle;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GiftExtraRewardMapper {
    int deleteByPrimaryKey(Long id);

    int insert(GiftExtraReward record);

    GiftExtraReward selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(GiftExtraReward record);

    List<GiftExtraReward> getGiftExtraRewards(@Param("giftId") Long giftId);

    Map<String, Object> getGiftExtraRewardNum(@Param("id") Integer giftId);

    UserTitle getUserTitle(@Param("userId") Long userId, @Param("titleCode") String titleCode);

    int insertUserTitle(UserTitle record);

    int updateUserTitle(UserTitle record);

}