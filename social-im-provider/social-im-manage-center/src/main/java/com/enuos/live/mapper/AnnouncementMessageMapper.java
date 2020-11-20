package com.enuos.live.mapper;

import com.enuos.live.pojo.AnnouncementMessage;

public interface AnnouncementMessageMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AnnouncementMessage record);

    AnnouncementMessage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(AnnouncementMessage record);

    AnnouncementMessage selectByRangeId(Integer rangeId);

    Integer selectByRangeIdCount(Integer rangeId);

}