package com.enuos.live.mapper;

import com.enuos.live.pojo.UserAccountAttachPO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface UserAccountAttachPOMapper {

    Integer insert(UserAccountAttachPO accountAttach);

    Integer update(UserAccountAttachPO accountAttach);

    // 查询账户附属信息
    UserAccountAttachPO getAttach(@Param("id") Integer id, @Param("userId") Long userId);

    Integer upSurplusAmount(@Param("rp") Integer rp, @Param("rpId") Long rpId);

    Integer upUserAccountAttachAmount(@Param("gold") Long gold, @Param("userId") Long userId);

    // 查询金币和钻石数量
    Map<String, Object> getBalance(@Param("id") Integer id, @Param("userId") Long userId);

    Map<String, Object> getUserMsg(@Param("id") Integer id, @Param("userId") Long userId);

    int selectByPrimaryKeyGoldRatio(@Param("gold") Long gold);
}
