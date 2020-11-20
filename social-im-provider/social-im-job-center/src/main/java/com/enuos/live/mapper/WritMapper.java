package com.enuos.live.mapper;

import com.enuos.live.pojo.Title;
import com.enuos.live.pojo.Writ;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Description 令状
 * @Author wangyingjie
 * @Date 2020/10/15
 * @Modified
 */
public interface WritMapper {
    
    /** 
     * @Description: 获取令状信息
     * @Param: [currentDate]
     * @Return: com.enuos.live.pojo.Writ 
     * @Author: wangyingjie
     * @Date: 2020/10/15 
     */ 
    Writ getWrit(@Param("templateCode") String templateCode, @Param("date") LocalDate date);

    /** 
     * @Description: 获取排行榜
     * @Param: [writCode, limit] 
     * @Return: java.util.List<java.lang.Long> 
     * @Author: wangyingjie
     * @Date: 2020/10/16 
     */ 
    List<Long> getRankUser(@Param("taskCode") String taskCode, @Param("limit") Integer limit);

    /** 
     * @Description: 排行榜奖励
     * @Param: [writCode] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/10/19 
     */ 
    List<Map<String, Object>> getRankReward(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode);

    /**
     * @Description: 获取称号
     * @Param: [userId, titleCode]
     * @Return: com.enuos.live.pojo.Title
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    Title getTitle(@Param("userId") Long userId, @Param("titleCode") String titleCode);

    /**
     * @Description: 保存称号
     * @Param: [title]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    void saveTitle(Title title);

    /**
     * @Description: 修改称号
     * @Param: [title]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/19
     */
    void updateTitle(Title title);
}
