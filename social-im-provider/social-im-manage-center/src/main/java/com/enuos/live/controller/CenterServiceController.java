package com.enuos.live.controller;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.annotations.NoRepeatSubmit;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.cipher.AESEncoder;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.manager.VerifyEnum;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.CenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName CenterServiceController
 * @Description: TODO 服务中心
 * @Author xubin
 * @Date 2020/5/7
 * @Version V1.0
 **/
@Api("服务中心")
@Slf4j
@RestController
@RequestMapping("/centerService")
public class CenterServiceController {

    @Autowired
    private CenterService centerService;

    @ApiOperation("联系我们")
    @Cipher
    @PostMapping("/contactUs")
    public Result contactUs() {

        return Result.success();
    }

    @ApiOperation("用户、隐私协议，软件介绍")
    @Cipher
    @PostMapping("/agreement")
    public Result agreement(@RequestBody Map<String, Integer> params) {

        return centerService.agreement(params.get("type"));
    }

    @ApiOperation("消息提醒")
    @Cipher
    @PostMapping("/messageTips")
    public Result messageTips() {

        return Result.success();
    }


    @ApiOperation("版本检查更新")
    @Cipher
    @PostMapping("/versionCheck")
    public Result versionCheck(@RequestBody Map<String, String> params) {
        return centerService.versionCheck(params.get("version"), params.get("platform"));
    }

    @ApiOperation("后台更新新增版本")
    @Cipher
    @PostMapping("/versionUp")
    public Result versionInsert(@Validated @RequestBody VersionApp versionApp, BindingResult bindingResult) {
        if (!Objects.isNull(versionApp.getId())) {
            return centerService.versionUp(versionApp);
        }
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return centerService.addVersionApp(versionApp);
    }

    @ApiOperation("相关问题")
    @Cipher
    @PostMapping("/solveAProblem")
    public Result solveAProblem(@RequestBody Map<String, Integer> params) {
        return centerService.solveAProblem(params.get("pageNum"), params.get("pageSize"), params.get("keyword"));
    }

    @ApiOperation("获取所有相关问题")
    @Cipher
    @PostMapping("/findAllSolveProblem")
    public Result findAllSolveProblem() {

        return centerService.findAllSolveProblem();
    }

    @ApiOperation("意见反馈")
    @Cipher
    @PostMapping("/feedback")
    public Result feedback(@Valid @RequestBody Feedback feedback, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return centerService.feedback(feedback);
    }


    @ApiOperation("获取用户设置")
    @Cipher
    @PostMapping("/getUserSettings")
    public Result getUserSettings(@RequestBody Base base) {

        return centerService.getUserSettings(base.userId);
    }

    @NoRepeatSubmit(lockTime = 1)
    @ApiOperation("修改用户设置")
    @Cipher
    @PostMapping("/upUserSettings")
    public Result upUserSettings(@RequestBody UserSettings record) {

        return centerService.upUserSettings(record);
    }

    @CrossOrigin
    @ApiOperation("保存调查问卷")
    @Cipher
    @PostMapping("/saveQuestionnaire")
    public Result saveQuestionnaire(/*@Valid*/ @RequestBody Questionnaire questionnaire/*, BindingResult bindingResult*/) {

        //        if (bindingResult.hasErrors()) {
//            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
//        }

        return centerService.saveQuestionnaire(questionnaire);
    }

    // 加密
    @PostMapping("/encrypt")
    public Result encrypt(@RequestBody Map<String, Object> params) {

        Long signature = MapUtil.getLong(params, "signature");
        String data = MapUtil.getStr(params, "data");
        Map map = MapUtil.get(params, "data", Map.class);
        String jsonString = JSONObject.toJSONString(map);
        log.info("调试:要加密的参数=[{}]", jsonString);
        return Result.success(AESEncoder.encrypt(signature, jsonString));

    }

    // 解密
    @PostMapping("/decrypt")
    public Result decrypt(@RequestBody Map<String, Object> params) {
        Long signature = MapUtil.getLong(params, "signature");
        String data = MapUtil.getStr(params, "data");
        log.info("要解密的参数,signature=[{}], data=[{}]", signature, data);
        return Result.success(AESEncoder.decrypt(signature, data));

    }
}
