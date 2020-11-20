package com.enuos.live.controller;

import com.enuos.live.utils.sensitive.DFAWordUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 词库
 * @Author wangyingjie
 * @Date 2020/7/24
 * @Modified
 */
@Slf4j
@Api("词库")
@RestController
@RequestMapping("/word")
public class WordController {
    
    @Autowired
    private DFAWordUtils dfaWordUtils;

    /**
     * ==========[内部服务]==========
     */

    /** 
     * @Description: 匹配敏感词，全匹配
     * @Param: [words]
     * @Return: java.lang.Boolean 
     * @Author: wangyingjie
     * @Date: 2020/7/24 
     */ 
    @ApiOperation(value = "匹配敏感词", notes = "匹配敏感词")
    @GetMapping("/matchWords")
    public Boolean matchWords(String words) {
        return dfaWordUtils.matchWords(words);
    }
}
