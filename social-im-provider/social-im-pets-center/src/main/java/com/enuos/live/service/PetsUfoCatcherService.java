package com.enuos.live.service;

import com.enuos.live.result.Result;

public interface PetsUfoCatcherService {

    Result catcherPrice(Integer catcherId);

    Result getPetsUfoCatcher(Integer catcherId, Integer drawNum, Long userId);

    Result previewPrize(Integer catcherId);
}
