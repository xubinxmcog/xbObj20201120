package com.enuos.live.mapper;

import com.enuos.live.pojo.ExceptionInfo;
import com.enuos.live.pojo.ExceptionServiceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExceptionInfoMapper {
    int deleteByPrimaryKey(Long id);

    // 删除一个月前的异常统计数据
    int deleteByCreateTime();

    int insert(ExceptionInfo record);

    ExceptionInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ExceptionInfo record);

    List<Map<String, Object>> getExceptionMsg(@Param("startTime") String startTime,
                                              @Param("endTime") String endTime);


    int insertServiceException(@Param("records") List<ExceptionServiceInfo> records);

    String getServiceName(@Param("code") String code);
}