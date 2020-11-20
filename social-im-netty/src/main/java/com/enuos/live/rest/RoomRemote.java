package com.enuos.live.rest;

import com.enuos.live.rest.impl.RoomRemoteFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(contextId = "roomRemote", name = "SOCIAL-IM-VOICE", fallback = RoomRemoteFallback.class)
public interface RoomRemote {

    /**
     * 根据房间Id查询房间信息
     * @param roomId 房间Id
     * @return
     */
    @RequestMapping(value = "/feign/room/info", method = RequestMethod.GET)
    Result getRoomInfoResult(@RequestParam("roomId") Long roomId);

    /**
     * 根据房间Id查询房间成员ID
     * @param roomId 房间Id
     * @return
     */
    @GetMapping("/feign/room/open/getRoomUserIdList")
    List<Long> getRoomUserIdList(@RequestParam("roomId") Long roomId);

    /**
     * 是否禁言 1: 禁言 0: 没禁言
     * @param roomId
     * @param userId
     * @return
     */
    @GetMapping("/feign/room/open/isBanned")
    Map<String, Object> isBanned(@RequestParam("roomId") Long roomId, @RequestParam("userId") Long userId);


    /**
     * 退出房间
     */
    @GetMapping("/feign/room/out")
    Result out(@RequestParam("roomId") Long roomId, @RequestParam("userId") Long userId);

    /**
     * 下播
     */
    @GetMapping("/feign/room/endBroadcast")
    Result endBroadcast(@RequestParam("roomId") Long roomId);

    /**
     * 语音房任务达成
     */
    @GetMapping("/feign/room/roomTaskHandler")
    int roomTaskHandler(@RequestParam("code") String code,
                                @RequestParam("userId") Long userId,
                                @RequestParam("roomId") Long roomId);

    /**
     * 获取房间人数 作废
     * @param roomId
     * @return
     */
    @GetMapping("/feign/room/getOnlineNum")
    Integer getOnlineNum(@RequestParam("roomId") Long roomId);

    /**
     * 房间用户角色
     * @return
     */
    @GetMapping("/feign/room/getVoiceRoomUserRole")
    Integer getVoiceRoomUserRole(@RequestParam(value = "userId") Long userId, @RequestParam(value = "roomId") Long roomId);

    /**
     * 获取用户语音房进场特效
     * @param userId
     * @return
     */
    @GetMapping("/feign/room/enterEffects")
    String enterEffects(@RequestParam("userId") Long userId);
}
