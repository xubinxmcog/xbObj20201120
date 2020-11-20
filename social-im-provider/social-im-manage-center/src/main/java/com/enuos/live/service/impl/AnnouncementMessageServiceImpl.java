package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.AnnouncementMessageMapper;
import com.enuos.live.pojo.AnnouncementMessage;
import com.enuos.live.result.Result;
import com.enuos.live.service.AnnouncementMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName AnnouncementMessageServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/20
 * @Version V1.0
 **/
@Slf4j
@Service
public class AnnouncementMessageServiceImpl implements AnnouncementMessageService {

    @Autowired
    private AnnouncementMessageMapper announcementMessageMapper;

    @Override
    public Result upAnnouncementMessage(AnnouncementMessage announcementMessage) {

//        Integer flag;
//        Integer rangeIdCount = announcementMessageMapper.selectByRangeIdCount(announcementMessage.getRangeId());
//        if (rangeIdCount < 1) {
//            flag =
        announcementMessageMapper.insert(announcementMessage);
//        } else {
//            flag = announcementMessageMapper.updateByPrimaryKeySelective(announcementMessage);
//        }

//        if (flag > 0) {
        return Result.success();
//        } else {
//            log.error("更新失败{}", "AnnouncementMessageServiceImpl.upAnnouncementMessage");
//            return Result.error(ErrorCode.ERROR_OPERATION);
//        }

    }

    @Override
    public Result queryList(AnnouncementMessage announcementMessage) {
        return null;
    }

    @Override
    public Result queryDetail(Long id) {
        return null;
    }

    @Override
    public Result userQueryDetail(Integer rangeId) {
        if (null == rangeId || rangeId == 0) {
            log.error("参数为空，{}", "AnnouncementMessageServiceImpl.userQueryDetail");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        return Result.success(announcementMessageMapper.selectByRangeId(rangeId));
    }
}
