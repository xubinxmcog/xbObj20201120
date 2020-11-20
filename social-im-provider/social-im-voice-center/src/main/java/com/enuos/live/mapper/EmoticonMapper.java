package com.enuos.live.mapper;

import com.enuos.live.pojo.Emoticon;

import java.util.List;

public interface EmoticonMapper {

    List<Emoticon> getEmoticonList();

    Emoticon selectByPrimaryKey(Long emId);

}