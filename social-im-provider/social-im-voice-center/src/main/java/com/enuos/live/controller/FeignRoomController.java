package com.enuos.live.controller;

import com.enuos.live.annotations.RoomUserActivityNote;
import com.enuos.live.pojo.RoomPO;
import com.enuos.live.result.Result;
import com.enuos.live.service.RoomService;
import com.enuos.live.server.RoomTaskServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName FeignController
 * @Description: TODO
 * @Author xubin
 * @Date 2020/9/22
 * @Version V2.0
 **/
@Slf4j
@Api("语音房服务内部调用接口")
@RestController
@RequestMapping("/feign/room")
public class FeignRoomController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomTaskServer roomTaskServer;

    /**
     * 获取房间信息
     */
    @ApiOperation(value = "获取房间信息", notes = "房间信息")
    @GetMapping("/info")
    public Result info(RoomPO roomPO) {
        return roomService.info(roomPO);
    }

    /**
     * 根据房间Id查询房间成员ID
     *
     * @param roomId 房间Id
     * @return
     */
    /**
     * [OPEN]获取房间用户id
     */
    @ApiOperation(value = "获取房间用户id", notes = "获取房间用户id")
    @GetMapping("/open/getRoomUserIdList")
    public List<Long> getRoomUserIdList(@RequestParam("roomId") Long roomId) {
        return roomService.getRoomUserIdList(roomId);
    }

    /**
     * [OPEN]是否在禁言期 1: 禁言 0: 没禁言
     */
    @ApiOperation(value = "是否在禁言期", notes = "是否在禁言期")
    @GetMapping("/open/isBanned")
    public Map<String, Object> isBanned(@RequestParam("roomId") Long roomId, @RequestParam("userId") Long userId) {
        return roomService.isBanned(roomId, userId);
    }


    /**
     * 退出房间
     */
    @ApiOperation(value = "退出房间", notes = "退出房间")
    @GetMapping("/out")
    public Result out(RoomPO roomPO) {
        return roomService.out(roomPO);
    }

    /**
     * 下播
     */
    @ApiOperation(value = "下播", notes = "下播")
    @GetMapping("/endBroadcast")
    public Result endBroadcast(RoomPO roomPO) {
        return roomService.endBroadcast(roomPO);
    }

    /**
     * 语音房任务达成
     */
    /**
     * 语音房任务达成
     */
    @RoomUserActivityNote(activityType = 1)
    @ApiOperation(value = "语音房任务达成")
    @GetMapping("/roomTaskHandler")
    public int roomTaskHandler(@RequestParam("code") String code,
                                @RequestParam("userId") Long userId,
                                @RequestParam("roomId") Long roomId) {
        roomTaskServer.taskHandler(code, userId, roomId);
        return 1;
    }

    /**
     * 获取房间人数
     *
     * @param roomId
     * @return
     */
    @ApiOperation(value = "获取房间人数")
    @GetMapping("/getOnlineNum")
    public Integer getOnlineNum(@RequestParam("roomId") Long roomId) {
        return roomService.getOnlineNum(roomId);
    }

    /**
     * 房间用户角色
     *
     * @return
     */
    @ApiOperation("房间用户角色")
    @GetMapping("/getVoiceRoomUserRole")
    public Integer getVoiceRoomUserRole(@RequestParam(value = "userId") Long userId,
                                        @RequestParam(value = "roomId") Long roomId) {
        return roomService.getVoiceRoomUserRole(userId, roomId);
    }

    /**
     * 获取用户语音房进场特效
     * @param userId
     * @return
     */
    @ApiOperation(value = "获取用户语音房进场特效")
    @GetMapping("/enterEffects")
    public String enterEffects(@RequestParam("userId") Long userId) {
        return roomService.enterEffects(userId);
    }
}
