package com.enuos.live.mapper;

import com.enuos.live.pojo.Questionnaire;

public interface QuestionnaireMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Questionnaire record);

    Questionnaire selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Questionnaire record);

}