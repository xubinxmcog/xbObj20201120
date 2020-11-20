package com.enuos.live.rest.fallback;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.rest.UploadRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 错降级处理
 *
 * @author WangCaiWen Created on 2020/4/9 11:13
 */
@Slf4j
@Component
public class UploadRemoteFallback implements UploadRemote {

  @Override
  public Result uploadChatFile(MultipartFile file) {
    return Result.error(ErrorCode.CHAT_PARAM_NULL);
  }

}
