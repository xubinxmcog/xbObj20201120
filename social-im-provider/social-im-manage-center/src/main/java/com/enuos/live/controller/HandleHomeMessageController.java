package com.enuos.live.controller;

import com.enuos.live.annotations.OperateLog;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.common.constants.ReleaseTypeEnum;
import com.enuos.live.dto.AdMessageDTO;
import com.enuos.live.manager.VerifyEnum;
import com.enuos.live.pojo.AdMessage;
import com.enuos.live.result.Result;
import com.enuos.live.service.AdMessageService;
import com.enuos.live.service.factory.AdMessageServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * @ClassName HandleHomeMessageController
 * @Description: TODO 首页信息管理
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Api("举报管理中心")
@Slf4j
@RestController
@RequestMapping("/manage")
public class HandleHomeMessageController {

    @ApiOperation("广告发布")
    @Cipher
    @PostMapping("/editAd")
    @OperateLog(operateMsg = "广告发布")
    public Result editAd(@Valid @RequestBody AdMessage adMessage, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        AdMessageService adMessageService = AdMessageServiceFactory.getAdMessageService(ReleaseTypeEnum.RELEASE_AD);
        if (ObjectUtils.isEmpty(adMessageService)) {
            return Result.error(VerifyEnum.ERROR_CODE_202.getCode(), "实例化失败！");
        }
        return adMessageService.insert(adMessage);
    }

    @ApiOperation("广告修改")
    @Cipher
    @PostMapping("/editUpAd")
    @OperateLog(operateMsg = "广告修改")
    public Result editUpAd(@RequestBody AdMessageDTO adMessage) {
        AdMessageService adMessageService = AdMessageServiceFactory.getAdMessageService(ReleaseTypeEnum.RELEASE_AD);
        if (ObjectUtils.isEmpty(adMessageService)) {
            return Result.error(VerifyEnum.ERROR_CODE_202.getCode(), "实例化失败！");
        }
        return adMessageService.update(adMessage);
    }

    @ApiOperation("广告列表查询")
    @Cipher
    @PostMapping("/queryAdList")
    public Result queryAdList(@RequestBody AdMessageDTO adMessage) {
        AdMessageService adMessageService = AdMessageServiceFactory.getAdMessageService(ReleaseTypeEnum.RELEASE_AD);
        if (ObjectUtils.isEmpty(adMessageService)) {
            return Result.error(VerifyEnum.ERROR_CODE_202.getCode(), "实例化失败！");
        }
        return adMessageService.queryList(adMessage);
    }

    @ApiOperation("广告详情查询")
    @Cipher
    @PostMapping("/queryDetail/{id}")
    public Result queryDetail(@RequestBody Map<String, Long> params) {
        AdMessageService adMessageService = AdMessageServiceFactory.getAdMessageService(ReleaseTypeEnum.RELEASE_AD);
        return adMessageService.queryDetail(params.get("id"));
    }
}
