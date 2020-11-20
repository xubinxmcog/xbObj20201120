package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.dto.EmoticonDTO;
import com.enuos.live.dto.GiftGiveDTO;
import com.enuos.live.mapper.EmoticonMapper;
import com.enuos.live.pojo.Base;
import com.enuos.live.result.Result;
import com.enuos.live.service.GiftService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * @ClassName GiftController
 * @Description: TODO 礼物
 * @Author xubin
 * @Date 2020/6/17
 * @Version V2.0
 **/
@Api("礼物")
@Slf4j
@RestController
@RequestMapping("/gift")
public class GiftController {

    @Autowired
    private GiftService giftService;

    @Autowired
    private EmoticonMapper emoticonMapper;


    @ApiOperation("查询礼物列表")
    @Cipher
    @PostMapping("/getList")
    public Result getList() {
        return giftService.getList();
    }

    @ApiOperation("用户礼物券列表")
    @Cipher
    @PostMapping("/getUserCouponList")
    public Result getUserCouponList(@RequestBody Base base) {
        return giftService.getUserCouponList(base.userId);
    }

    @ApiOperation("赠送礼物")
    @Cipher
    @PostMapping("/give")
    public Result give(@Validated @RequestBody GiftGiveDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return giftService.give(dto);
    }

    @ApiOperation("赠送礼物数量字典")
    @Cipher
    @PostMapping("/getGiveNumList")
    public Result getGiveNumList(@RequestBody Map<String, Long> params) {
        return giftService.getGiveNumList(params.get("giftId"));
    }

    @ApiOperation("表情包列表")
    @Cipher
    @PostMapping("/getEmoticonList")
    public Result getEmoticonList() {
        return Result.success(emoticonMapper.getEmoticonList());
    }

    @ApiOperation("赠送表情包")
    @Cipher
    @PostMapping("/giveEmoticon")
    public Result giveEmoticon(@Validated @RequestBody EmoticonDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return giftService.giveEmoticon(dto);
    }

}
