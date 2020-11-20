package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.RoomService;
import com.enuos.live.server.RoomTaskServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 语音房
 * @Author wangyingjie
 * @Date 10:49 2020/5/11
 * @Modified
 */
@Slf4j
@Api("语音房")
@RestController
@RequestMapping("/room")
public class RoomController {

    private String userID; // 用户ID

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomTaskServer roomTaskServer;

    /**
     * 初始化房间信息
     */
    @ApiOperation(value = "初始化房间信息", notes = "初始化房间信息")
    @Cipher
    @PostMapping("/init")
    public Result init(@RequestBody RoomPO roomPO) {
        return roomService.init(roomPO);
    }

    /**
     * 开播
     */
    @ApiOperation(value = "开播", notes = "开播")
    @Cipher
    @PostMapping("/startBroadcast")
    public Result startBroadcast(@Validated @RequestBody RoomPO roomPO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return roomService.startBroadcast(roomPO);
    }

    /**
     * 下播
     */
    @ApiOperation(value = "下播", notes = "下播")
    @Cipher
    @PostMapping("/endBroadcast")
    public Result endBroadcast(@RequestBody RoomPO roomPO) {
        return roomService.endBroadcast(roomPO);
    }

    /**
     * 房间列表
     */
    @ApiOperation(value = "房间列表", notes = "房间列表")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody RoomPO roomPO) {
        return roomService.list(roomPO);
    }

    /**
     * 进入房间
     */
    @ApiOperation(value = "进入房间", notes = "进入房间")
    @Cipher
    @PostMapping("/in")
    public Result in(@RequestBody RoomPO roomPO) {
        return roomService.in(roomPO);
    }

    /**
     * 退出房间
     */
    @ApiOperation(value = "退出房间", notes = "退出房间")
    @Cipher
    @PostMapping("/out")
    public Result out(@RequestBody RoomPO roomPO) {
        return roomService.out(roomPO);
    }

    /**
     * 获取房间信息
     */
    @ApiOperation(value = "获取房间信息", notes = "房间信息")
    @Cipher
    @PostMapping("/info")
    public Result info(@RequestBody RoomPO roomPO) {
        return roomService.info(roomPO);
    }

    /**
     * 获取房间基础信息
     */
    @ApiOperation(value = "获取房间基础信息")
    @Cipher
    @PostMapping("/baseInfo")
    public Result baseInfo(@RequestBody RoomPO roomPO) {
        return roomService.baseInfo(roomPO.getRoomId(), roomPO.getUserId());
    }

    @ApiOperation(value = "排麦/取消排麦")
    @Cipher
    @PostMapping("/queueMic")
    public Result queueMic(@Validated @RequestBody MicPO micPO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return roomService.queueMic(micPO);
    }

    @ApiOperation(value = "修改麦序")
    @Cipher
    @PostMapping("/upQueueMic")
    public Result upQueueMic(@Validated @RequestBody MicPO micPO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return roomService.upQueueMic(micPO);
    }

    @ApiOperation(value = "排麦列表")
    @Cipher
    @PostMapping("/getMicList")
    public Result getMicList(@RequestBody MicPO micPO) {
        return roomService.getMicList(micPO.getRoomId());
    }

    @ApiOperation(value = "房间座位配置")
    @Cipher
    @PostMapping("/seat")
    public Result seat(@Validated @RequestBody SeatPO seatPO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return roomService.seat(seatPO);
    }

    @ApiOperation(value = "房间座位配置-抱人上麦/踢人")
    @Cipher
    @PostMapping("/seatPerson")
    public Result seatPerson(@Validated @RequestBody SeatPO seatPO) {
        if (Objects.isNull(seatPO.getRoomId())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "房间号不能为空");
        }
        return roomService.seat(seatPO);
    }

    @ApiOperation(value = "加入、离开座位")
    @Cipher
    @PostMapping("/joinSeat")
    public Result joinSeat(@Validated @RequestBody SeatPO seatPO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return roomService.joinSeat(seatPO);
    }

    /**
     * 房间用户关系-关注房间/拉黑/禁言
     */
    @ApiOperation(value = "房间用户关系", notes = "房间用户关系")
    @Cipher
    @PostMapping("/relation")
    public Result relation(@RequestBody RelationPO relationPO) {
        return roomService.relation(relationPO);
    }

    /**
     * 设置房间角色
     */
    @ApiOperation(value = "设置房间角色", notes = "房间角色")
    @Cipher
    @PostMapping("/role")
    public Result role(@RequestBody RolePO rolePO) {
        return roomService.role(rolePO);
    }

    /**
     * 获取房间角色列表
     */
    @ApiOperation(value = "获取房间角色列表")
    @Cipher
    @PostMapping("/getRole")
    public Result getRole(@RequestBody RolePO rolePO) {
        return roomService.getRole(rolePO);
    }

    /**
     * 禁言黑名单
     */
    @ApiOperation(value = "禁言黑名单", notes = "禁言黑名单")
    @Cipher
    @PostMapping("/bannedList")
    public Result bannedList(@RequestBody RoomPO roomPO) {
        return roomService.bannedList(roomPO);
    }

    /**
     * 房间设置
     */
    @ApiOperation(value = "房间设置", notes = "房间设置")
    @Cipher
    @PostMapping("/setting")
    public Result setting(@RequestBody RoomPO roomPO) {
        return roomService.setting(roomPO);
    }

    /**
     * 房间用户信息
     */
    @ApiOperation(value = "房间用户信息")
    @Cipher
    @PostMapping("/userInfo")
    public Result userInfo(@RequestBody RoomPO roomPO) {
        return roomService.userInfo(roomPO);
    }

    /**
     * [OPEN]获取房间用户id
     */
    @ApiOperation(value = "获取房间用户id", notes = "获取房间用户id")
    @Cipher
    @PostMapping("/open/getRoomUserIdList")
    public List<Long> getRoomUserIdList(@RequestBody RoomPO roomPO) {
        return roomService.getRoomUserIdList(roomPO.getRoomId());
    }

    /**
     * 获取房间用户列表
     */
    @ApiOperation(value = "获取房间用户列表", notes = "获取房间用户id")
    @Cipher
    @PostMapping("/getRoomUserList")
    public Result getRoomUserList(@RequestBody RoomPO roomPO) {
        return roomService.getRoomUserList(roomPO.getRoomId());
    }

    /**
     * 获取房间座位列表
     */
    @ApiOperation(value = "获取房间用户列表", notes = "获取房间用户id")
    @Cipher
    @PostMapping("/getRoomSeatList")
    public Result getRoomSeatList(@RequestBody RoomPO roomPO) {
        return roomService.getRoomSeatList(roomPO.getRoomId());
    }

    /**
     * [OPEN]是否在禁言期
     */
    @ApiOperation(value = "是否在禁言期", notes = "是否在禁言期")
    @Cipher
    @PostMapping("/open/isBanned")
    public Map<String, Object> isBanned(@RequestBody Map<String, Long> params) {
        return roomService.isBanned(params.get("roomId"), params.get("userId"));
    }

    @ApiOperation(value = "房间贡献榜")
    @Cipher
    @PostMapping("/dedicate/ranking")
    public Result dedicateRanking(@RequestBody Map<String, Long> params) {
        return roomService.dedicateRanking(MapUtils.getLong(params, "roomId"), MapUtils.getInteger(params, "pageSize"));
    }

    @ApiOperation(value = "获取语音房主题")
    @Cipher
    @PostMapping("/getRoomTheme")
    public Result getRoomTheme() {
        return roomService.getRoomTheme();
    }

    /**
     * 语音房任务达成
     */
    @ApiOperation(value = "语音房任务达成")
    @Cipher
    @PostMapping("/roomTaskHandler")
    public void roomTaskHandler(@RequestBody Map<String, Object> params) {
        String code = MapUtils.getString(params, "code");
        Long userId = MapUtils.getLong(params, "userId");
        Long roomId = MapUtils.getLong(params, "roomId");

        roomTaskServer.taskHandler(code, userId, roomId);
    }

    @ApiOperation(value = "获取房间人数")
    @Cipher
    @PostMapping("/getOnlineNum")
    public Integer getOnlineNum(@RequestBody Map<String, Long> params) {
        return roomService.getOnlineNum(params.get("roomId"));
    }

    /**
     * 房间背景列表
     *
     * @param page
     * @return
     */
    @ApiOperation("房间背景列表")
    @Cipher
    @PostMapping("/getBackgrounds")
    public Result getVoiceRoomBackgrounds(@RequestBody Page page) {
        return roomService.getVoiceRoomBackgrounds(page.pageNum, page.pageSize);
    }

    /**
     * 房间用户角色
     *
     * @param roomPO
     * @return
     */
    @ApiOperation("房间用户角色")
    @Cipher
    @PostMapping("/getVoiceRoomUserRole")
    public Integer getVoiceRoomUserRole(@RequestBody RoomPO roomPO) {
        return roomService.getVoiceRoomUserRole(roomPO.getUserId(), roomPO.getRoomId());
    }

}