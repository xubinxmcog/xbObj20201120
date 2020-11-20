package com.enuos.live.mapper;

import com.enuos.live.pojo.Agreement;

public interface AgreementMapper {

    Agreement selectByType(Integer type);

}