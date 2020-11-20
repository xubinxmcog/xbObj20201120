package com.enuos.live.service;

import com.enuos.live.pojo.AnnouncementMessage;
import com.enuos.live.result.Result;

public interface AnnouncementMessageService {

    Result upAnnouncementMessage(AnnouncementMessage announcementMessage);

    Result queryList(AnnouncementMessage announcementMessage);

    Result queryDetail(Long id);

    Result userQueryDetail(Integer rangeId);
}
