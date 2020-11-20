package com.enuos.live.service;

import com.enuos.live.pojo.PKPO;
import com.enuos.live.result.Result;

public interface PlayerKillingService {

    Result createPK(PKPO po);

    Result getPK(Long roomId);

    Result poll(PKPO po);
}
