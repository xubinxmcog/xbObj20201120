package com.enuos.live.mapper;

import com.enuos.live.pojo.BusinessPayMsg;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface BusinessPayMsgMapper {

    int insert(BusinessPayMsg record);

    BusinessPayMsg selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BusinessPayMsg record);

    BusinessPayMsg selectByOpenId(@Param("partnerTradeNo") String partnerTradeNo);

    Map<String, Object> getByOrderIdMoney(@Param("orderId") String orderId);

    int upMoneyGet(@Param("orderStatus") Integer orderStatus, @Param("orderId") String orderId);

}