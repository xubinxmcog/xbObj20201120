package com.enuos.live.rest;

import com.enuos.live.config.FeignSpringFormEncoder;
import com.enuos.live.rest.fallback.UploadRemoteFallback;
import com.enuos.live.result.Result;
import feign.codec.Encoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 远程调用
 *
 * @author WangCaiWen Created on 2020/4/9 11:12
 */
@Component
@FeignClient(name = "SOCIAL-IM-MANAGE", url = "${upload.url}", configuration = UploadRemote.FeignSupportConfig.class, fallback = UploadRemoteFallback.class)
public interface UploadRemote {

  /**
   * 上传文件
   *
   * @param file 文件
   * @return 结果
   */
  @RequestMapping(method = RequestMethod.POST, value = "/files/uploadFile", produces = {
      MediaType.APPLICATION_JSON_UTF8_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  Result uploadChatFile(@RequestPart("file") MultipartFile file);


  /**
   * 配置FEIGN文件上传
   */
  class FeignSupportConfig {

    @Bean
    public Encoder feignFormEncoder() {
      return new FeignSpringFormEncoder();
    }

  }
}
