package com.enuos.live.feign;

import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Component
@FeignClient(name = "SOCIAL-IM-ACTIVITY")
public interface ActivityFeign {

    /**
     * @Description: 丹枫迎秋 任务处理
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    @PostMapping("/qiuri/open/handler")
    Result openHandler(@RequestBody Map<String, Object> params);
}
