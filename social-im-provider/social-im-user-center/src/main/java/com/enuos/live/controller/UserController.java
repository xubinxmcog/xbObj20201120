package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description 个人中心
 * @Author wangyingjie
 * @Date 17:18 2020/4/1
 * @Modified 2020/7/3
 */
@Slf4j
@Api("个人中心")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * @Description: 用户A获取用户B，是否好友[单向好友关系]
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    @ApiOperation(value = "用户A获取用户B，是否好友[单向好友关系]", notes = "用户A获取用户B，是否好友[单向好友关系]")
    @Cipher
    @PostMapping("/getStranger")
    public Result getStranger(@RequestBody User user) {
        return userService.getStranger(user.userId, user.getToUserId());
    }

    /**
     * @Description: 用户A获取用户B详情
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    @ApiOperation(value = "用户A获取用户B详情", notes = "用户A获取用户B详情")
    @Cipher
    @PostMapping("/getStrangerDetail")
    public Result getStrangerDetail(@RequestBody User user) {
        return userService.getStrangerDetail(user.userId, user.getToUserId());
    }

    /**
     * @Description: 获取个人基础信息
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取个人基础信息", notes = "获取个人基础信息")
    @Cipher
    @PostMapping("/getBase")
    public Result getBase(@RequestBody User user) {
        return userService.getBase(user);
    }

    /**
     * @Description: 获取身价
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    @ApiOperation(value = "获取身价", notes = "获取身价")
    @Cipher
    @PostMapping("/worthList")
    public Result worthList() {
        return userService.worthList();
    }

    /**
     * @Description: 修改性别需要的金币
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    @ApiOperation(value = "修改性别需要的金币", notes = "修改性别需要的金币")
    @Cipher
    @PostMapping("/toUpdateSex")
    public Result toUpdateSex() {
        return userService.toUpdateSex();
    }

    /**
     * @Description: 修改个人基础信息
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "修改个人基础信息", notes = "修改个人基础信息")
    @Cipher
    @PostMapping("/updateBase")
    public Result updateBase(@RequestBody User user) {
        try {
            return userService.updateBase(user);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }

    /**
     * @Description: 获取主页综合信息
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取主页综合信息", notes = "获取主页综合信息")
    @Cipher
    @PostMapping("/getDetail")
    public Result getDetail(@RequestBody User user) {
        return userService.getDetail(user);
    }

    /**
     * @Description: 获取主页称号
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/4
     */
    @ApiOperation(value = "获取主页称号", notes = "获取主页称号")
    @Cipher
    @PostMapping("/title")
    public Result title(@RequestBody User user) {
        return userService.title(user);
    }

    /**
     * @Description: 获取附近的人
     * @Param: [nearby]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "附近的用户", notes = "获取附近的用户列表")
    @Cipher
    @PostMapping("/nearbyList")
    public Result nearbyList(@RequestBody Nearby nearby) {
        return userService.nearbyList(nearby);
    }

    /**
     * @Description: 获取屏蔽动态的用户列表
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @ApiOperation(value = "获取屏蔽动态的用户列表", notes = "获取屏蔽动态的用户列表")
    @Cipher
    @PostMapping("/shieldList")
    public Result shieldList(@RequestBody Shield shield) {
        return userService.shieldList(shield);
    }

    /**
     * @Description: 取消屏蔽
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @ApiOperation(value = "获取屏蔽动态的用户列表", notes = "获取屏蔽动态的用户列表")
    @Cipher
    @PostMapping("/unShield")
    public Result unShield(@RequestBody Shield shield) {
        return userService.unShield(shield);
    }

    /**
     * @Description: 获取钻石金币
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/28
     */
    @ApiOperation(value = "获取钻石金币", notes = "获取钻石金币")
    @Cipher
    @PostMapping("/getCurrency")
    public Result getCurrency(@RequestBody Base base) {
        return userService.getCurrency(base.userId);
    }

    /**
     * @Description: 官网后台充值，输入userId，获取用户信息以校验
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "官网后台充值，输入userId，获取用户信息以校验", notes = "官网后台充值，输入userId，获取用户信息以校验")
    @Cipher
    @PostMapping("/getUserForRecharge")
    public Result getUserForRecharge(@RequestBody Base base) {
        return userService.getUserForRecharge(base.userId);
    }

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 获取钻石金币
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/28
     */
    @ApiOperation(value = "获取钻石金币", notes = "获取钻石金币")
    @GetMapping("/open/getCurrencyForServer")
    public Result getCurrencyForServer(Long userId) {
        return userService.getCurrency(userId);
    }

    /**
     * @Description: 获取个人基础信息
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取个人基础信息", notes = "获取个人基础信息")
    @GetMapping("/open/getBaseForServer")
    public Result getBaseForServer(User user) {
        return userService.getBase(user);
    }

    /**
     * @Description: 获取用户基本信息
     * @Param: [userId, friendId]
     * @Return: java.util.Map<java.lang.String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取陌生人", notes = "获取陌生人")
    @GetMapping("/open/getUserBase")
    public Map<String, Object> getUserBase(Long userId, Long friendId) {
        return userService.getUserBase(userId, friendId);
    }

    /**
     * @Description: AB关系[0：否；1：是]
     * @Param: [userId, toUserId, flag]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "AB关系", notes = "AB关系")
    @GetMapping("/open/getRelation")
    public Integer getRelation(Long userId, Long toUserId, Integer flag) {
        return userService.getRelation(userId, toUserId, flag);
    }

    /**
     * @Description: 获取用户昵称，性别，头像，账号等级
     * @Param: [userIdList]
     * @Return: java.util.List<java.util.Map                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               <                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               java.lang.String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取用户昵称，性别，头像，账号等级", notes = "获取用户昵称，性别，头像，账号等级")
    @PostMapping("/open/getUserList")
    public List<Map<String, Object>> getUserList(@RequestBody List<Long> userIdList) {
        return userService.getUserList(userIdList);
    }

    /**
     * @Description: 获取头像框，聊天框
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/27
     */
    @ApiOperation(value = "获取头像框，聊天框", notes = "获取头像框，聊天框")
    @GetMapping("/open/getUserFrame")
    public Map<String, Object> getUserFrame(@RequestParam("userId") Long userId) {
        return userService.getUserFrame(userId);
    }

    /**
     * @Description: 获取用户昵称，性别，头像，账号等级,vip等级
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String   ,   java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "获取用户昵称，性别，头像，账号等级,vip等级")
    @GetMapping("/open/getUserMsg")
    public Map<String, Object> getUserMsg(@RequestParam("userId") Long userId) {
        return userService.getUserMsg(userId);
    }

    /**
     * @Description: 获取昵称的字符串以','拼接
     * @Param: [userIdList]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取昵称的字符串以','拼接", notes = "获取昵称的字符串以','拼接")
    @PostMapping("/open/getNickName")
    public String getNickName(@RequestBody List<Long> userIdList) {
        return userService.getNickName(userIdList);
    }

    /**
     * @Description: 获取用户ID，会员或者非会员或全部
     * @Param: [isMember]
     * @Return: java.util.List<java.lang.Long>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取用户ID，会员或者非会员或全部", notes = "获取用户ID，会员或者非会员或全部")
    @GetMapping("/open/getUserIdList")
    public List<Long> getUserIdList(Integer isMember) {
        return userService.getUserIdList(isMember);
    }

}
