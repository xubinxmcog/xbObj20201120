package com.enuos.live.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.constant.CodeEnum;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.IdCardFeign;
import com.enuos.live.mapper.CardMapper;
import com.enuos.live.pojo.Card;
import com.enuos.live.result.Result;
import com.enuos.live.service.CardService;
import com.enuos.live.service.CommonService;
import com.enuos.live.utils.IdCardUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;

/**
 * @Description
 * @Author wangyingjie
 * @Date 15:20 2020/5/8
 * @Modified
 */
@Slf4j
@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private IdCardFeign idCardFeign;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CardMapper cardMapper;

    /**
     * @Description: 身份证二要素核验
     * @Param: [card]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    @Transactional
    public Result idCard(Card card) {
        if (card.getName().length() > 20) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "姓名过长");
        }

        Long userId = card.getUserId();
        Integer cardType = card.getCardType();
        String name = card.getName();
        String idCard = card.getIdCard();

        // 身份证校验
        if (!IdCardUtil.isIdcard(idCard)) {
            return Result.error(ErrorCode.IDCARD_ILLEGAL);
        }

        int bandCount = cardMapper.getBandCount(card);
        // 该身份证是否达到绑定次数限制[5次]
        if (bandCount > CodeEnum.CODE4.getCode()) {
            return Result.error(ErrorCode.IDCARD_AUTHENTICATION);
        }

        // 该人员可能修改姓名及性别，故每次都应该去核验
        JSONObject userInfo = JSONObject.parseObject(idCardFeign.idenAuthentication(new HashMap<String, String>() {
            {
                put("idNo", idCard);
                put("name", name);
            }
        }));
        String status = userInfo.getString("respCode");

        if (!Objects.equals(status, "0000")) {
            // 认证失败
            return Result.error(ErrorCode.EXCEPTION_CODE, userInfo.getString("respMessage"));
        }

        Card nCard = new Card();
        nCard.setUserId(userId);
        nCard.setPhone(MapUtils.getString(commonService.getUserBase(userId, "phone"), "phone"));
        nCard.setName(name);
        nCard.setSex(userInfo.getString("sex"));
        nCard.setCardType(cardType);
        nCard.setIdCard(idCard);

        int result = cardMapper.save(nCard);
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

    /**
     * @Description: 是否认证
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Integer isAuthentication(Long userId) {
        return cardMapper.isAuthentication(userId) == null ? 0 : 1;
    }

}
