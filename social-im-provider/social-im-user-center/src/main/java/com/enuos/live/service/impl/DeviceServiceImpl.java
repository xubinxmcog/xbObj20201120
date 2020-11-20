package com.enuos.live.service.impl;

import com.enuos.live.mapper.DeviceMapper;
import com.enuos.live.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description 设备
 * @Author wangyingjie
 * @Date 2020/8/10
 * @Modified
 */
@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceMapper deviceMapper;

    /**
     * @Description: 保存设备号
     * @Param: [userId, number, type]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/8/10
     */
    @Override
    @Transactional
    public int save(Long userId, String number, Integer type) {
        if (StringUtils.isBlank(number) || type == null) {
            log.error("Device save number error, no data");
            return 0;
        }

        if (deviceMapper.isExists(userId, number, type) != null) {
            log.error("Device save number error, is exists");
            return 0;
        }

        return deviceMapper.save(userId, number, type);
    }
}
