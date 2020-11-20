package com.enuos.live.rest.impl;

import com.enuos.live.rest.UploadRemote;
import com.enuos.live.result.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO 熔断处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/9 - 2020/7/28
 */

@Component
public class UploadRemoteFallback implements UploadRemote {

  @Override
  public Result uploadChatFile(MultipartFile file, String h, String w, String folder) {
    return Result.error(-1, "上传失败");
  }

}
