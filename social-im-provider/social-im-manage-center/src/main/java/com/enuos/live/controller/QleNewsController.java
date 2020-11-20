package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.Page;
import com.enuos.live.pojo.QleNews;
import com.enuos.live.result.Result;
import com.enuos.live.service.QleNewsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName QleNewsController
 * @Description: TODO 资讯
 * @Author xubin
 * @Date 2020/7/22
 * @Version V2.0
 **/
@Slf4j
@Api("资讯")
@RestController
@RequestMapping("/7leNews")
public class QleNewsController {

    @Autowired
    private QleNewsService qleNewsService;

    /**
     * @MethodName: insert
     * @Description: TODO 新增资讯
     * @Param: [record]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    @Cipher
    @PostMapping("/insert")
    public Result insert(@Validated @RequestBody QleNews record, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return qleNewsService.insert(record);
    }

    /**
     * @MethodName: insert
     * @Description: TODO 更新资讯
     * @Param: [record]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    @Cipher
    @PostMapping("/up")
    public Result updateByPrimaryKeySelective(@RequestBody QleNews record) {
        return qleNewsService.updateByPrimaryKeySelective(record);
    }

    /**
     * @MethodName: insert
     * @Description: TODO 资讯列表
     * @Param: [pageNum, pageSize]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    @Cipher
    @PostMapping("/list")
    public Result selectNewsList(@RequestBody Page page) {

        return qleNewsService.selectNewsList(page.pageNum, page.pageSize);
    }

    /**
     * @MethodName: insert
     * @Description: TODO 资讯搜索
     * @Param: [title]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    @Cipher
    @PostMapping("/query")
    public Result selectNewsTitle(@RequestBody Map<String, String> params) {

        return qleNewsService.selectNewsTitle(params.get("title"));

    }

    /**
     * @MethodName: insert
     * @Description: TODO 资讯详情
     * @Param: [id]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    @Cipher
    @PostMapping("/detail")
    public Result detail(@RequestBody Map<String, Integer> params) {
        return qleNewsService.selectByPrimaryKey(params.get("id"));
    }

}
