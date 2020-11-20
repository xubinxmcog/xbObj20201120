package com.enuos.live.service;

import com.enuos.live.dto.TipOffsDTO;
import com.enuos.live.pojo.TipOffs;
import com.enuos.live.result.Result;

/**
 * @ClassName TipOffsService
 * @Description: TODO 举报管理
 * @Author xubin
 * @Date 2020/4/14
 * @Version V1.0
 **/
public interface TipOffsService {


    Result insert(TipOffs tipOffs);

    Result queryTipOffList(TipOffsDTO dto);

    Result queryByIdTipOff(TipOffsDTO dto);

    Result handleInfo(TipOffs tipOffs);

    String getCode();
}
