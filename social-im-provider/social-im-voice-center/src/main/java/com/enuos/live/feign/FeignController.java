package com.enuos.live.feign;

import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangCaiWen
 * Created on 2020/4/24 9:31
 */
@Slf4j
@RestController
@RequestMapping("/feign")
public class FeignController {

    @RequestMapping(value = "/voice/getVoiceCover", method = RequestMethod.GET)
    public Result getVoiceCover(@RequestParam("voiceId") Integer voiceId) {
        return null;
    }

}
