package com.enuos.live.service;

import com.enuos.live.pojo.ProductBackpack;
import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

public interface ProductBackpackService {

    Result upProductBackpack(ProductBackpack productBackpack);

    Result use(Integer id, Long userId, Integer amount);

    List gameDecorate(Long userId, Integer gameCode);

    Result addBackpack(Map<String, Object> param);

    Result getUserOrnaments(Long userId);

}
