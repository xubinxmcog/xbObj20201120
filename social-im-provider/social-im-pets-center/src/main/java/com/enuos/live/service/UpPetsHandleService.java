package com.enuos.live.service;

import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.result.Result;

public interface UpPetsHandleService {

    Result updatePetsHandle(PetsInfo petsInfo);

    String getCode();


}
