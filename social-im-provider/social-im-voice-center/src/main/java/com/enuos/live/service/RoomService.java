package com.enuos.live.service;

import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description 语音房
 * @Author wangyingjie
 * @Date 12:48 2020/5/11
 * @Modified
 */
public interface RoomService {

    /**
     * 初始化房间信息
     * @param roomPO
     * @return
     */
    Result init(RoomPO roomPO);

    /**
     * 开播
     * @param roomPO
     * @return
     */
    Result startBroadcast(RoomPO roomPO);

    /**
     * 下播
     * @param roomPO
     * @return
     */
    Result endBroadcast(RoomPO roomPO);

    /**
     * 列表
     * @param roomPO
     * @return
     */
    Result list(RoomPO roomPO);

    /**
     * 进入房间
     * @param roomPO
     * @return
     */
    Result in(RoomPO roomPO);

    /**
     * 退出房间
     * @param roomPO
     * @return
     */
    Result out(RoomPO roomPO);

    /**
     * 房间信息
     * @param roomPO
     * @return
     */
    Result info(RoomPO roomPO);

    /**
     * 房间基础信息
     * @param roomId
     * @return
     */
    Result baseInfo(Long roomId, Long userId);
    /**
     * seat[join, leave, lock]
     * @param seatPO
     * @return
     */
    Result seat(SeatPO seatPO);

    /**
     * @MethodName: joinSeat
     * @Description: TODO 加入座位
     * @Param: [seatPO]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 18:04 2020/6/28
    **/
    Result joinSeat(SeatPO seatPO);

    /**
     * @MethodName: queueMic
     * @Description: TODO 排麦上麦下麦
     * @Param: [seatPO]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:06 2020/6/29
    **/
    Result queueMic(MicPO micPO);

    /**
     * @MethodName: upQueueMic
     * @Description: TODO 修改麦序
     * @Param: [micPO]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:10 2020/7/2
    **/
    Result upQueueMic(MicPO micPO);

    /**
     * 排麦列表
     * @param roomId
     * @return
     */
    Result getMicList(Long roomId);

    /**
     * 房间用户关系
     * @param relationPO
     * @return
     */
    Result relation(RelationPO relationPO);

    /**
     * 角色
     * @param rolePO
     * @return
     */
    Result role(RolePO rolePO);
    /**
     * 角色
     * @param rolePO
     * @return
     */
    Result getRole(RolePO rolePO);

    /**
     * 禁言黑名单
     * @param roomPO
     * @return
     */
    Result bannedList(RoomPO roomPO);

    /**
     * 房间设置
     * @param roomPO
     * @return
     */
    Result setting(RoomPO roomPO);

    /**
     * 房间用户信息
     * @param roomPO
     * @return
     */
    Result userInfo(RoomPO roomPO);

    /**
     * [OPEN]获取房间用户id
     * @param roomId
     * @return
     */
    List<Long> getRoomUserIdList(Long roomId);

    /**
     * 获取房间用户列表
     * @param roomId
     * @return
     */
    Result getRoomUserList(Long roomId);

    /**
     * 获取房间座位列表
     * @param roomId
     * @return
     */
    Result getRoomSeatList(Long roomId);

    /**
     * [OPEN]是否在禁言期
     * @param roomId
     * @param userId
     * @return
     */
    Map<String, Object> isBanned(Long roomId, Long userId);


    Result dedicateRanking(Long roomId, Integer pageSize);

    Result getRoomTheme();

    Integer getOnlineNum(Long roomId);

    /**
     * @MethodName: getVoiceRoomBackgrounds
     * @Description: TODO 语音房背景图列表
     * @Param: [pageNum, pageSize]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:07 2020/8/19
    **/
    Result getVoiceRoomBackgrounds(Integer pageNum, Integer pageSize);

    /**
     * @MethodName: getVoiceRoomUserRole
     * @Description: TODO 房间用户角色
     * @Param: [userId, roomId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:43 2020/8/27
    **/
    Integer getVoiceRoomUserRole(Long userId, Long roomId);

    /**
     * @MethodName: enterEffects
     * @Description: TODO 获取用户语音房进场特效
     * @Param: [userId]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 17:38 2020/10/26
     **/
    String enterEffects(Long userId);

    /**
     * @MethodName: startMPUTask
     * @Description: TODO 阿里云旁路转推开始任务
     * @Param: [channelId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:01 2020/6/24
    **/
    Result startMPUTask(String channelId);

    /**
     * @MethodName: startMPUTask
     * @Description: TODO 调用StopMPUTask停止任务
     * @Param: [taskId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:01 2020/6/24
     **/
    Result stopMPUTask(String taskId);

    /**
     * @MethodName: createToken
     * @Description: TODO 阿里云直播生成token
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:09 2020/6/24
    **/
    Result createToken(Map<String, String> params);


    Result getMPUTaskStatus(String taskId);

}
