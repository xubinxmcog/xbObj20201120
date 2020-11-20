package com.enuos.live.utils;

import com.alibaba.fastjson.JSON;
import com.enuos.live.file.pojo.ImgResult;
import com.google.common.io.Files;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @ClassName ZimgUtils
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/22
 * @Version V1.0
 **/
@Slf4j
public class ZimgUtils {

    private final static String host = "http://192.168.0.50:4869/upload";

    /**
     * @MethodName: upload
     * @Description: TODO
     * @Param: [multipartFile, ext:文件类型]
     * @Return: com.enuos.live.file.pojo.ImgResult
     * @Author: xubin
     * @Date: 2020/4/22
     **/
    public static ImgResult upload(MultipartFile multipartFile, String ext) throws IOException {
        HttpRequest request = HttpRequest
                .post(host)
                .body(multipartFile.getBytes(), ext);

        HttpResponse response = request.send()
                .acceptEncoding(Charsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ImgResult result = JSON.parseObject(response.bodyText(), ImgResult.class);
        log.info("文件上传结果===>{}", result);

        return result;
    }
}
