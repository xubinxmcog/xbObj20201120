package com.enuos.live.feign.impl;

import com.enuos.live.feign.UpFileFeign;
import org.springframework.stereotype.Component;

/**
 * @ClassName UpFileFeignFallback
 * @Description: TODO 文件操作异常回退
 * @Author xubin
 * @Date 2020/7/8
 * @Version V2.0
 **/
@Component
public class UpFileFeignFallback implements UpFileFeign {

    @Override
    public void delRoomChatFile(String fileName) {

    }
}
