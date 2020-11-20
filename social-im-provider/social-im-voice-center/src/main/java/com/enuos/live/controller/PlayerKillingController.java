package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.PKPO;
import com.enuos.live.result.Result;
import com.enuos.live.service.PlayerKillingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PlayerKillingController
 * @Description: TODO PK
 * @Author xubin
 * @Date 2020/7/6
 * @Version V2.0
 **/
@Api("PK")
@Slf4j
@RestController
@RequestMapping("/pk")
public class PlayerKillingController {

    @Autowired
    private PlayerKillingService playerKillingService;

    @ApiOperation("创建PK")
    @Cipher
    @PostMapping("/createPK")
    public Result createPK(@Validated @RequestBody PKPO po, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return playerKillingService.createPK(po);
    }

    @ApiOperation("查询PK")
    @Cipher
    @PostMapping("/getPK")
    public Result getPK(@RequestBody Map<String, Long> params) {
        return playerKillingService.getPK(params.get("roomId"));
    }

    @ApiOperation("PK投票")
    @Cipher
    @PostMapping("/poll")
    public Result poll(@RequestBody PKPO po) {
        return playerKillingService.poll(po);
    }
}
