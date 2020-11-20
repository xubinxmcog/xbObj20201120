package com.enuos.live.mapper;

import com.enuos.live.pojo.RoomDetailVO;
import com.enuos.live.pojo.RoomPO;
import com.enuos.live.pojo.RoomVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 12:48 2020/5/11
 * @Modified
 */
public interface RoomMapper {

    /**
     * 获取房间
     *
     * @return
     */
    Map<String, Object> getBaseByUserId(Long userId);

    /**
     * 修改
     *
     * @param roomPO
     * @return
     */
    int update(RoomPO roomPO);

    /**
     * 开播/下播
     *
     * @param roomId
     * @return
     */
    int updateBroadcast(@Param("roomId") Long roomId, @Param("isBroadcast") Integer isBroadcast);

    /**
     * 房间信息
     *
     * @param roomId
     * @return
     */
    RoomDetailVO ownerRoomInfo(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 创建房间
     *
     * @param roomPO
     * @return
     */
    int save(RoomPO roomPO);

    /**
     * 是否存在
     *
     * @param roomId
     * @return
     */
    Integer isExists(Long roomId);

    /**
     * 列表
     *
     * @param roomPO
     * @return
     */
    List<RoomVO> getList(RoomPO roomPO);

    /**
     * 是否开播
     *
     * @param roomId
     * @return
     */
    int isBroadcast(Long roomId);

    /**
     * 获取密码
     *
     * @param roomId
     * @return
     */
    String getPassword(Long roomId);

    /**
     * 房间信息
     *
     * @param roomPO
     * @return
     */
    RoomDetailVO getInfo(RoomPO roomPO);

    /**
     * 是否房主
     *
     * @param roomId
     * @param userId
     * @return
     */
    int isOwner(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 是否房主或管理员权限
    Integer isChmod(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * @MethodName: getMicMode
     * @Description: TODO 获取排麦模式
     * @Param: [roomId]
     * @Return: int
     * @Author: xubin
     * @Date: 18:09 2020/6/28
     **/
    int getMicMode(@Param("roomId") Long roomId);

    /**
     * @MethodName: getRoomEmptySeat
     * @Description: TODO 获取房间空位置
     * @Param: [roomId]
     * @Return: int
     * @Author: xubin
     * @Date: 17:24 2020/6/29
     **/
    Integer getRoomEmptySeat(@Param("roomId") Long roomId);

    /**
     * @MethodName: getRoomEmptySeat
     * @Description: TODO 获取房间空位置-忽略锁
     * @Param: [roomId]
     * @Return: java.lang.Integer
     * @Author: xubin
     * @Date: 13:56 2020/7/1
     **/
    Integer getRoomEmptySeatUnlock(@Param("roomId") Long roomId);

    Map<String, Object> getBaseInfo(@Param("roomId") Long roomId, @Param("userId") Long userId);

    //获取房间内所有关注用户
    List<Map<String, Object>> getRelationUsers(@Param("roomId") Long roomId);

    Map<String, Object> getRoomRelationNum(@Param("roomId") Long roomId);

    List<Map<String, Object>> getRoomTheme();

    int saveVoiceRoomBroadcastTime(@Param("roomId") Long roomId);

    int upVoiceRoomBroadcastTime(@Param("roomId") Long roomId);

    String enterEffects(@Param("userId") Long userId);

    // 语音房用户活跃度
    int saveVoiceRoomUserActivity(Map<String, Object> map);

    // 语音房用户活跃度
    int updateVoiceRoomUserActivity(Map<String, Object> map);

}
