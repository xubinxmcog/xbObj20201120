package com.enuos.live.rest;

import com.enuos.live.rest.fallback.VoiceRemoteFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author WangCaiWen Created on 2020/4/24 9:34
 */
@Component
@FeignClient(name = "VOICE-CENTER", fallback = VoiceRemoteFallback.class)
public interface VoiceRemote {

  /**
   * 获得语音房封面
   *
   * @param voiceId the Voice_Id
   * @return is Voice_Cover
   */
  @RequestMapping(value = "/feign/voice/getVoiceCover", method = RequestMethod.GET)
  Result getVoiceCover(@RequestParam("voiceId") Long voiceId);
}
