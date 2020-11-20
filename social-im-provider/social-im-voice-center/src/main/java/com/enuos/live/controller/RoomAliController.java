package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName RoomAliController
 * @Description: TODO 语音房阿里云旁路转推
 * @Author xubin
 * @Date 2020/7/21
 * @Version V2.0
 **/
@Slf4j
@Api("语音房阿里云旁路转推")
@RestController
@RequestMapping("/ali/room")
public class RoomAliController {

    @Autowired
    private RoomService roomService;

    @ApiOperation(value = "阿里云推拉流生成token")
    @Cipher
    @PostMapping("/createToken")
    public Result createToken(@RequestBody Map<String, String> params) {
        return roomService.createToken(params);
    }

    @ApiOperation(value = "阿里云旁路转推开始任务")
    @Cipher
    @PostMapping("/startMPUTask")
    public Result startMPUTask(@RequestBody Map<String, String> params) {
        String channelId = params.get("channelId");
        return roomService.startMPUTask(channelId);
    }

    @ApiOperation(value = "调用StopMPUTask停止任务")
    @Cipher
    @PostMapping("/stopMPUTask")
    public Result stopMPUTask(@RequestBody Map<String, String> params) {
        String taskId = params.get("taskId");
        return roomService.stopMPUTask(taskId);
    }

    @ApiOperation(value = "调用GetMPUTaskStatus获取任务状态")
    @PostMapping("/getMPUTaskStatus")
    public Result getMPUTaskStatus(@RequestBody Map<String, String> params) {
        String taskId = params.get("taskId");
        return roomService.getMPUTaskStatus(taskId);
    }
}
