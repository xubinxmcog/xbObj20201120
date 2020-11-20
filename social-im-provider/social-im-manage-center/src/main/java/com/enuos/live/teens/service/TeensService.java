package com.enuos.live.teens.service;

import com.enuos.live.result.Result;
import com.enuos.live.teens.pojo.TeensModel;

public interface TeensService {

    Result savePwd(TeensModel teensModel);

    TeensModel completePassword(TeensModel teensModel);

    Result updatePwd(Long userId, String oldPwd, String newPwd);

    Result openORclose(TeensModel teensModel);

    Result getTeensModel(TeensModel teensModel);

    Result verification(Long userId, String oldPwd);
}
