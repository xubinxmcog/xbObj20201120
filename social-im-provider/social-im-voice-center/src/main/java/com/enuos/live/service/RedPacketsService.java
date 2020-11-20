package com.enuos.live.service;

import com.enuos.live.pojo.RedPacketsSend;
import com.enuos.live.pojo.RobVO;
import com.enuos.live.result.Result;

public interface RedPacketsService {

    Result send(RedPacketsSend redPacketsSend);

    Result rob(RobVO robVO);
}
