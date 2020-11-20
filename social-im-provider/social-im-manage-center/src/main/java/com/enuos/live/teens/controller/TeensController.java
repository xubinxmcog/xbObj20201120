package com.enuos.live.teens.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.result.Result;
import com.enuos.live.teens.pojo.TeensModel;
import com.enuos.live.teens.service.TeensService;
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
 * @ClassName TeensController
 * @Description: TODO 青少年模式
 * @Author xubin
 * @Date 2020/5/6
 * @Version V1.0
 **/
@Api("青少年模式管理")
@RestController
@RequestMapping("/teens")
@Slf4j
public class TeensController {

    @Autowired
    private TeensService teensService;

    // 移到 getUserSettings 接口
//    @ApiOperation("获取青少年模式状态")
//    @PostMapping("/getTeensModel")
//    public Result getTeensModel(@RequestBody TeensModel teensModel) {
//        return teensService.getTeensModel(teensModel);
//    }

    @ApiOperation("设置密码并开启青少年模式")
    @Cipher
    @PostMapping("/open")
    public Result open(@RequestBody TeensModel teensModel) {
        Long userId = teensModel.getUserId();
        String password = teensModel.getTeensPwd();
//        String pwd = teensModel.getPwd();

        // 参数校验
        if (null == userId || null == password) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        return teensService.savePwd(teensModel);
    }

    @ApiOperation(value = "验证密码")
    @Cipher
    @PostMapping("/verificationPwd")
    public Result verificationPwd(@RequestBody Map<String, String> params) {
        Long userId = Long.valueOf(params.get("userId"));
        String oldPwd = params.get("oldPwd");
        log.info("验证密码入参: userId = [{}], oldPwd =[{}]", userId, oldPwd);
        if (null == userId) {
            return Result.error(2000, "userId is null");
        }
        return teensService.verification(userId, oldPwd);
    }


    @ApiOperation(value = "修改密码")
    @Cipher
    @PostMapping("/modifyPWD")
    public Result modifyPWD(@RequestBody Map<String, String> params) {
        String oldPWD = params.get("oldPwd");
        String newPwd = params.get("newPwd");
        Long userId = Long.valueOf(params.get("userId"));

        return teensService.updatePwd(userId, oldPWD, newPwd);
    }
//    @ApiOperation(value = "开启青少年模式")
//    @PostMapping("/open")
//    public Result open(@RequestBody TeensModel teensModel){
//        teensModel.setStatus(1);
//        return teensService.openORclose(teensModel);
//    }

    @ApiOperation(value = "关闭青少年模式")
    @Cipher
    @PostMapping("/close")
    public Result close(@RequestBody TeensModel teensModel) {
        teensModel.setStatus(0);
        return teensService.openORclose(teensModel);
    }
}


