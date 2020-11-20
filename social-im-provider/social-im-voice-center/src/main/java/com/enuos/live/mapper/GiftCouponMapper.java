package com.enuos.live.mapper;

import com.enuos.live.pojo.GiftCoupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GiftCouponMapper {
    int deleteByPrimaryKey(Long couponId);

    // 删除过期的券
    int deleteOverdue();

    int delUserGiftCoupon(@Param("userId") Long userId, @Param("giftCouponId") String giftCouponId, @Param("count") Integer count);

    int insert(GiftCoupon record);

    GiftCoupon selectByPrimaryKey(Long couponId);

    int updateByPrimaryKeySelective(GiftCoupon record);

    List<Map<String, Object>> selectUserGiftCouponList(@Param("userId") Long userId);

    int giftCount(@Param("userId") Long userId, @Param("giftCouponId") String giftCouponId);

}