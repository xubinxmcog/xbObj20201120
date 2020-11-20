package com.enuos.live.service;

import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.result.Result;

import java.util.Map;

public interface NestService {

    Result me(Long userId);

    Result list(Long userId);

    Result upPetsInfo(PetsInfo petsInfo);

    Result upUserBackpack(Long userId, String upTarget, String tips);

    Result unlock(Map<String, Object> params);

    Result upDown(Map<String, Object> params);

    Result pieces(Long userId);

    Result petsExchange(Map<String, Object> params);
}
