package com.enuos.live.controller;

import com.enuos.live.annotations.PetsTask;
import com.enuos.live.annotations.UserDynamic;
import com.enuos.live.dto.PetsDynamicDTO;
import com.enuos.live.service.PetsDynamicService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PetsDynamicController
 * @Description: TODO 宠物动态
 * @Author xubin
 * @Date 2020/10/19
 * @Version V2.0
 **/
@RestController
@RequestMapping("/dynamic")
public class PetsDynamicController {

    @Autowired
    private PetsDynamicService petsDynamicService;

    /**
     * @MethodName: get
     * @Description: TODO 获取动态
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:03 2020/10/19
     **/
    @PostMapping("/get")
    public Result get(@Validated @RequestBody PetsDynamicDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return petsDynamicService.get(dto);
    }

    /**
     * @MethodName: sign
     * @Description: TODO 标记动态
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:09 2020/10/19
     **/
    @PostMapping("/sign")
    public Result sign(@RequestBody PetsDynamicDTO dto) {
        return petsDynamicService.sign(dto);
    }

    /**
     * @MethodName: sign
     * @Description: TODO 获取好友列表
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:09 2020/10/19
     **/
    @GetMapping("/goodFriends")
    public Result goodFriends(@RequestParam Long userId) {
        return petsDynamicService.goodFriends(userId);
    }

    /**
     * @MethodName: sign
     * @Description: TODO 给好友喂食
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:09 2020/10/19
     **/
    @PetsTask(task = 3, describe = "帮好友喂食")
    @UserDynamic(describe = "帮你的宠物", dynamicType = 1)
    @PostMapping("/feed")
    public Result feed(@RequestBody Map<String, Object> params) {
        return petsDynamicService.feed(params);
    }

    /**
     * @MethodName: sign
     * @Description: TODO 给好友互动
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:09 2020/10/19
     **/
    @PetsTask(task = 4, describe = "帮好友互动")
    @UserDynamic(describe = "帮你的宠物", dynamicType = 2)
    @PostMapping("/toys")
    public Result toys(@RequestBody Map<String, Object> params) {
        return petsDynamicService.toys(params);
    }

    /**
     * @MethodName: sign
     * @Description: TODO 打招呼
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:09 2020/10/19
     **/
    @PetsTask(task = 5, describe = "打招呼")
    @UserDynamic(describe = "和你打了一个招呼", dynamicType = 3)
    @PostMapping("/hello")
    public Result hello(@RequestBody Map<String, Object> params) {
        return petsDynamicService.hello(params);
    }


}
