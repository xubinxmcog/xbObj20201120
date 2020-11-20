package com.enuos.live.mapper;

import com.enuos.live.pojo.HomeBusinessEntry;

import java.util.List;

public interface HomeBusinessEntryMapper {

    int insert(HomeBusinessEntry record);


    HomeBusinessEntry selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HomeBusinessEntry record);

    List<HomeBusinessEntry> getHomeBusinessEntry();

}