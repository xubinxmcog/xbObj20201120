package com.enuos.live.mapper;

import com.enuos.live.pojo.RolePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 房间角色
 * @Author wangyingjie
 * @Date 2020/5/18
 * @Modified
 */
public interface RoomRoleMapper {

    /**
     * 保存
     * @param roomId
     * @param userId
     * @param role
     */
    int save(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("role") Integer role);

    /**
     * 删除
     * @param roomId
     * @param userId
     * @param role
     */
    int delete(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("role") Integer role);

    List<Map<String,Object>> getRole(@Param("roomId") Long roomId, @Param("role") Integer role);

    Integer getAdmin(@Param("roomId") Long roomId, @Param("userId") Long userId);

}
