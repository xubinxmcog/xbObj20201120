package com.enuos.live.mapper;

import com.enuos.live.pojo.UserAccountAttach;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface UserAccountAttachMapper {

    Integer update(UserAccountAttach accountAttach);

    Integer updateDiamond(UserAccountAttach accountAttach);

    // 查询账户附属信息
//    UserAccountAttach getAttach(@Param("id") Integer id, @Param("userId") Long userId);

    // 查询金币和钻石数量
    Map<String, Object> getBalance(@Param("id") Integer id, @Param("userId") Long userId);

    int selectByPrimaryKeyGoldRatio(@Param("gold") Long gold);
}
