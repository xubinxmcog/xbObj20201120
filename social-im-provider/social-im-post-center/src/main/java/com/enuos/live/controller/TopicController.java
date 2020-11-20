package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Topic;
import com.enuos.live.result.Result;
import com.enuos.live.service.TopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 话题中心
 * @Author wangyingjie
 * @Date 2020/6/15
 * @Modified
 */
@Slf4j
@Api("话题中心")
@RestController
@RequestMapping("/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;
    
    /** 
     * @Description: 话题列表
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/15 
     */ 
    @ApiOperation(value = "话题列表", notes = "话题列表")
    @Cipher
    @PostMapping("/listWithPage")
    public Result listWithPage(@RequestBody Topic topic) {
        return topicService.listWithPage(topic);
    }

    /** 
     * @Description: 话题列表
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/16 
     */ 
    @ApiOperation(value = "话题列表", notes = "话题列表")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Topic topic) {
        return topicService.list(topic);
    }

    /**
     * @Description: 话题信息
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/16
     */
    @ApiOperation(value = "话题信息", notes = "话题信息")
    @Cipher
    @PostMapping("/info")
    public Result info(@RequestBody Topic topic) {
        return topicService.info(topic);
    }

}
