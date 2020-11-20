package com.enuos.live.mapper;

import com.enuos.live.pojo.ApplePayRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApplePayRecordMapper {

    int insert(ApplePayRecord record);

    int update(@Param("orderSn") Long orderSn, @Param("verification") Integer verification);

    int isExist(@Param("transactionId") String transactionId);

    List<ApplePayRecord> getApplePayRecords();
}