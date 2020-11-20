package com.enuos.live.service;

import com.enuos.live.common.constants.ReleaseTypeEnum;
import com.enuos.live.dto.AdMessageDTO;
import com.enuos.live.pojo.AdMessage;
import com.enuos.live.result.Result;

public interface AdMessageService {

    Result insert(AdMessage adMessage);

    Result update(AdMessageDTO adMessage);

    Result queryList(AdMessageDTO adMessage);

    Result queryDetail(Long id);

    Result userQueryDetail(Long id);

    ReleaseTypeEnum getCode();

}
