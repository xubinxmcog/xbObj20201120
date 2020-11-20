package com.enuos.live.rest.fallback;

import com.enuos.live.rest.VoiceRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author WangCaiWen Created on 2020/4/24 9:36
 */
@Slf4j
@Component
public class VoiceRemoteFallback implements VoiceRemote {

  @Override
  public Result getVoiceCover(Long voiceId) {
    return null;
  }
}
