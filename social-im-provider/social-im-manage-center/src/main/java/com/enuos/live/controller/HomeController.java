package com.enuos.live.controller;

import com.enuos.live.annotations.OperateLog;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.common.constants.ReleaseTypeEnum;
import com.enuos.live.dto.AdMessageDTO;
import com.enuos.live.manager.VerifyEnum;
import com.enuos.live.pojo.AnnouncementMessage;
import com.enuos.live.pojo.Page;
import com.enuos.live.result.Result;
import com.enuos.live.service.AdMessageService;
import com.enuos.live.service.AmpaignMessageService;
import com.enuos.live.service.AnnouncementMessageService;
import com.enuos.live.service.factory.AdMessageServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName HomeController
 * @Description: TODO 首页信息
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Slf4j
@Api("首页信息")
@RestController
@RequestMapping("/homeMessage")
public class HomeController {

    @Autowired
    private AnnouncementMessageService announcementMessageService;

    @Autowired
    private AmpaignMessageService ampaignMessageService;

    @ApiOperation("用户广告列表查询")
    @Cipher
    @PostMapping("/queryAdList")
    public Result queryAdList(@RequestBody AdMessageDTO adMessage) {
        AdMessageService adMessageService = AdMessageServiceFactory.getAdMessageService(ReleaseTypeEnum.RELEASE_AD);
        if (ObjectUtils.isEmpty(adMessageService)) {
            return Result.error(VerifyEnum.ERROR_CODE_202.getCode(), "实例化失败！");
        }
        adMessage.setStatus(1);
        adMessage.setReleaseTime(new Date());
        return adMessageService.queryList(adMessage);
    }

    @ApiOperation("用户广告详情查询")
    @Cipher
    @PostMapping("/queryDetail")
    public Result queryDetail(@RequestBody Map<String, Long> params) {
        AdMessageService adMessageService = AdMessageServiceFactory.getAdMessageService(ReleaseTypeEnum.RELEASE_AD);
        return adMessageService.userQueryDetail(params.get("id"));
    }

    @ApiOperation("获取公告")
    @Cipher
    @PostMapping("/getAnnouncement/{rangeId}")
    public Result getAnnouncement(@RequestBody Map<String, Integer> params) {

        return announcementMessageService.userQueryDetail(params.get("rangeId"));
    }

    @OperateLog(operateMsg = "更新公告")
    @ApiOperation("更新公告")
    @Cipher
    @PostMapping("/upAnnouncementMessage")
    public Result upAnnouncementMessage(@Valid @RequestBody AnnouncementMessage announcementMessage, BindingResult bindingResult) {
        log.info("announcementMessage=[{}]", announcementMessage.toString());
        if (bindingResult.hasErrors()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return announcementMessageService.upAnnouncementMessage(announcementMessage);
    }

    @ApiOperation("获取活动")
    @Cipher
    @PostMapping("/getCampaign")
    public Result getCampaign(@RequestBody Page page) {

        return ampaignMessageService.selectByList(page.pageNum, page.pageSize);
    }
    @ApiOperation("获取首页功能入口")
    @Cipher
    @PostMapping("/getBusinessEntry")
    public Result getBusinessEntry() {

        return ampaignMessageService.getBusinessEntry();
    }
}
