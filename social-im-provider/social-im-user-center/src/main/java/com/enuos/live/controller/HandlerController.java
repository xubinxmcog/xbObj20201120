package com.enuos.live.controller;

import com.enuos.live.service.HandlerService;
import com.enuos.live.task.Task;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/10/28
 * @Modified
 */
@Slf4j
@Api("处理器")
@RestController
@RequestMapping("/handler")
public class HandlerController {

    @Autowired
    private HandlerService handlerService;
    
    /** 
     * @Description: 日常
     * @Param: [task]
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    @ApiOperation(value = "日常", notes = "日常")
    @PostMapping("/dailyTask")
    public void dailyTask(@Validated @RequestBody Task task, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("handler/dailyTask message {}", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return;
        }
        handlerService.dailyTask(task);
    }
}
