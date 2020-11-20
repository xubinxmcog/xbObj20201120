package com.enuos.live.mapper;

import com.enuos.live.pojo.MicPO;
import com.enuos.live.pojo.RoomPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 用户
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
public interface RoomUserMapper {

    /**
     * 新增
     * @param roomId
     * @param userId
     * @return
     */
    int save(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 删除
     * @param roomPO
     * @return
     */
    int delete(RoomPO roomPO);

    /**
     * 删除房间用户
     * @param roomId
     * @return
     */
    int deleteRommUser(Long roomId);

    int delRoomUserOnlineNum(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 查询用户信息
     * @param userIdList
     * @return
     */
    List<Map<String, Object>> getBannedUser(@Param("userId") Long userId, @Param("userIdList") List<Long> userIdList);

    /**
     * 房间用户信息
     * @param roomId
     * @param userId
     * @param targetId
     * @return
     */
    Map<String, Object> getRoomUserInfo(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("targetId") Long targetId);

    /**
     * 修改
     * @param micPO
     * @return
     */
    int update(MicPO micPO);

    /**
     * [OPEN]获取房间内的所有用户
     * @param roomId
     * @return
     */
    List<Long> getRoomUserIdList(Long roomId);


    List<Map<String,Object>> queryUsersMsg(List<Long> list);

    List<Map<String,Object>> getRoomUsers(@Param("roomId") Long roomId);

    // 获取房间内所有用户
    List<Map<String,Object>> getRoomUserList(@Param("roomId") Long roomId);

    // 获取房间人数
    int getOnlineNum(@Param("roomId") Long roomId);

    // 判断用户是否在房间内
    Integer getUserIsRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

}
