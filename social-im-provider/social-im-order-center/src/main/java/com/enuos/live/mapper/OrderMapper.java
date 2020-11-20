package com.enuos.live.mapper;

import com.enuos.live.pojo.OrderMsg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {

    int insert(OrderMsg record);

    List<OrderMsg> selectByPrimaryKey(@Param("orderSn") Long orderSn, @Param("orderId") Long orderId,
                                      @Param("orderStatus") Integer orderStatus, @Param("rmbMethod") Integer rmbMethod,
                                      @Param("createTime") String createTime);

    List<OrderMsg> selectOrderStatus2(@Param("createTime") String createTime);

    int update(OrderMsg record);

    int updateHandle(@Param("orderSn") Long orderSn, @Param("isHandle") Integer isHandle);

}