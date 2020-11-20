package com.enuos.live.feign;

import com.enuos.live.feign.impl.UpFileFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "SOCIAL-IM-MANAGE", fallback = UpFileFeignFallback.class)
public interface UpFileFeign {

    @GetMapping(value = "/upFile/delRoomChatFile")
    void delRoomChatFile(@RequestParam("fileName") String fileName);
}
