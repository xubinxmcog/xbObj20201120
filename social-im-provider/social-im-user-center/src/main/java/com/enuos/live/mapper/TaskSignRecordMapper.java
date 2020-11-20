package com.enuos.live.mapper;

import com.enuos.live.pojo.TaskSign;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Description 签到记录
 * @Author wangyingjie
 * @Date 10:07 2020/4/9
 * @Modified
 */
public interface TaskSignRecordMapper {

    /**
     * @Description: 获取签到记录
     * @Param: [userId, begin, end]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/2
     */
    List<Map<String, Object>> getSignRecordList(@Param("userId") Long userId, @Param("begin") LocalDate begin, @Param("end") LocalDate end);

    /**
     * @Description: 是否存在签到记录
     * @Param: [taskSign]
     * @Return: Integer
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    Integer isExists(TaskSign taskSign);

    /**
     * @Description: 保存签到记录
     * @Param: [taskSign]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    int saveSignRecord(TaskSign taskSign);

}
