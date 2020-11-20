package com.enuos.live.mapper;

import com.enuos.live.pojo.TbChatEmoticon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbChatEmoticonMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TbChatEmoticon record);

    TbChatEmoticon selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbChatEmoticon record);

    List<TbChatEmoticon> selectEmoticon(@Param("categoryId") Integer categoryId);

}