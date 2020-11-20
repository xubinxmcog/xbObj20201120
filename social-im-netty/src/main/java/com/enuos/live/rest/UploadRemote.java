package com.enuos.live.rest;

import com.enuos.live.config.FeignSpringFormEncoder;
import com.enuos.live.rest.impl.UploadRemoteFallback;
import com.enuos.live.result.Result;
import feign.codec.Encoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO 管理中心.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/9 - 2020/7/29
 */

@Component
@FeignClient(name = "SOCIAL-IM-MANAGE", url = "${upload.url}", configuration = UploadRemote.FeignSupportConfig.class, fallback = UploadRemoteFallback.class)
public interface UploadRemote {

  /**
   * TODO 文件上传.
   *
   * @param file 文件
   * @param h 高度
   * @param w 宽度
   * @param folder 文件路径
   * @return 上传结果
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @RequestMapping(method = RequestMethod.POST, value = "/files/uploadFile", produces = {
      MediaType.APPLICATION_JSON_UTF8_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  Result uploadChatFile(@RequestPart("file") MultipartFile file, @RequestParam("h") String h, @RequestParam("w") String w, @RequestParam("folder") String folder);

  /**
   * FEIGN文件上传.
   */
  class FeignSupportConfig {

    @Bean
    public Encoder feignFormEncoder() {
      return new FeignSpringFormEncoder();
    }

  }
}
