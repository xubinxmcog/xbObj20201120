package com.enuos.live.controller;

import com.enuos.live.annotations.OperateLog;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.dto.TipOffsDTO;
import com.enuos.live.manager.VerifyEnum;
import com.enuos.live.pojo.TipOffs;
import com.enuos.live.result.Result;
import com.enuos.live.service.TipOffsService;
import com.enuos.live.service.factory.TipOffsServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @ClassName HandleTipOffsController
 * @Description: TODO  举报管理
 * @Author xubin
 * @Date 2020/4/15
 * @Version V1.0
 **/
@Api("举报管理中心")
@Slf4j
@RestController
@RequestMapping("/manage")
public class HandleTipOffsController {

    @ApiOperation("查询举报信息列表")
    @Cipher
    @PostMapping("/getTipOffList")
    public Result getList(@Valid @RequestBody TipOffsDTO dto, BindingResult bindingResult) {
        log.info("查询举报信息列表参数：{}", dto);
        if (bindingResult.hasErrors()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TipOffsService tipOffsService = getTipOffsService(dto.getTipOffType());
        if (null == tipOffsService) {
            return Result.error(VerifyEnum.ERROR_DOCE_203.getCode(), VerifyEnum.ERROR_DOCE_203.getMsg());
        }
        return tipOffsService.queryTipOffList(dto);

    }


    @ApiOperation("查询举报信息详情")
    @Cipher
    @PostMapping("/getTipOffDetail")
    public Result getTipOffDetail(@Valid @RequestBody TipOffsDTO dto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        TipOffsService tipOffsService = getTipOffsService(dto.getTipOffType());
        if (null == tipOffsService) {
            return Result.error(202, "查询类别不存在！");
        }
        return tipOffsService.queryByIdTipOff(dto);

    }

    @ApiOperation("处理用户举报信息")
    @Cipher
    @PostMapping("/handleUserInfo")
    @OperateLog(operateMsg = "处理用户举报信息")
    public Result handleUserInfo(@RequestBody TipOffs user) {

        if (user.getId() == null || user.getId() == 0) {
            log.error("id 不能为空！");
            return Result.error(203, "id cannot be empty");
        }

        TipOffsService tipOffsService = TipOffsServiceFactory.getTipOffsService("REPORT_USER");
        return tipOffsService.handleInfo(user);
    }

    @ApiOperation("处理群组举报信息")
    @Cipher
    @PostMapping("/handleGroupInfo")
    @OperateLog(operateMsg = "处理群组举报信息")
    public Result handleGroupInfo(@RequestBody TipOffs group) {
        if (group.getId() == null || group.getId() == 0) {
            log.error("id 不能为空！");
            return Result.error(203, "id cannot be empty");
        }

        TipOffsService tipOffsService = TipOffsServiceFactory.getTipOffsService("REPORT_GROUP");
        return tipOffsService.handleInfo(group);
    }

    @ApiOperation("处理房间举报信息")
    @Cipher
    @PostMapping("/handleRoomInfo")
    @OperateLog(operateMsg = "处理房间举报信息")
    public Result handleRoomInfo(@RequestBody TipOffs room) {
        if (room.getId() == null || room.getId() == 0) {
            log.error("id 不能为空！");
            return Result.error(203, "id cannot be empty");
        }

        TipOffsService tipOffsService = TipOffsServiceFactory.getTipOffsService("REPORT_ROOM");
        return tipOffsService.handleInfo(room);
    }

    private TipOffsService getTipOffsService(Integer tipOffType) {

        TipOffsService tipOffsService;
        // 1：用户，2：房间，3：群组
        switch (tipOffType) {
            case 1:
                tipOffsService = TipOffsServiceFactory.getTipOffsService("REPORT_USER");
                break;
            case 2:
                tipOffsService = TipOffsServiceFactory.getTipOffsService("REPORT_ROOM");
                break;
            case 3:
                tipOffsService = TipOffsServiceFactory.getTipOffsService("REPORT_GROUP");
                break;
            default:
                return null;
        }
        return tipOffsService;
    }
}
