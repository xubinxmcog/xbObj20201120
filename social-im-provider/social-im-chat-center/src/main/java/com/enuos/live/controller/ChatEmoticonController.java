package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.ChatEmoticonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ChatEmoticonController
 * @Description: TODO
 * @Author xubin
 * @Date 2020/11/9
 * @Version V2.0
 **/
@RestController
@RequestMapping("/emoticon")
public class ChatEmoticonController {

    @Autowired
    private ChatEmoticonService chatEmoticonService;

    /**
     * @MethodName: getEmoticon
     * @Description: TODO 查询聊天表情
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:26 2020/11/9
    **/
    @Cipher
    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Result getEmoticon(@RequestBody Map<String, Object> params) {

        return chatEmoticonService.getEmoticon(params);
    }
}
