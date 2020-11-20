package com.enuos.live.mapper;

import com.enuos.live.pojo.QleNews;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QleNewsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(QleNews record);

    QleNews selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QleNews record);

    List<QleNews> selectNewsList();

    List<QleNews> selectNewsTitle(@Param("title") String title);

}