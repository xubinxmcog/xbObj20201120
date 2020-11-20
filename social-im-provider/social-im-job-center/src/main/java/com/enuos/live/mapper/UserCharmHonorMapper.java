package com.enuos.live.mapper;

import com.enuos.live.pojo.TaskReward;
import com.enuos.live.pojo.UserCharmHonor;
import com.enuos.live.pojo.UserTitle;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserCharmHonorMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCharmHonor record);

    int insertUserTitle(UserTitle record);

    int updateUserTitle(UserTitle record);

    UserCharmHonor selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCharmHonor record);

    List<UserCharmHonor> getCharmDedicate(@Param("column") String column,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime,
                                          @Param("limit") Integer limit);

    List<TaskReward> getTaskRewards(@Param("taskCode") String taskCode);

    UserTitle getUserTitle(@Param("userId") Long userId, @Param("titleCode") String titleCode);

}