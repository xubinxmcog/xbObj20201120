package com.enuos.live.cipher;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.manager.CharsetEnum;
import com.enuos.live.pojo.Params;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @Description 数据转换
 * @Author wangyingjie
 * @Date 2020/9/14
 * @Modified
 */
public class DecryptInputMessage implements HttpInputMessage {

    private HttpInputMessage inputMessage;

    private String charset;

    public DecryptInputMessage(HttpInputMessage inputMessage, String charset) {
        this.inputMessage = inputMessage;
        this.charset = charset;
    }

    /**
     * @Description: 参数解密重新封装RequestBody参数
     * @Param: []
     * @Return: java.io.InputStream
     * @Author: wangyingjie
     * @Date: 2020/9/14
     */
    @Override
    public InputStream getBody() throws IOException {
        InputStream inputStream = inputMessage.getBody();
        String body = StreamUtils.copyToString(inputStream, Charset.forName(CharsetEnum.UTF8.getUppercase()));
        if (StringUtils.isNotBlank(body)) {
            Params params = JSONObject.parseObject(body, Params.class);
            body = AESEncoder.decrypt(params.getSignature(), params.getData());
        }
        return new ByteArrayInputStream(body.getBytes(charset));
    }

    @Override
    public HttpHeaders getHeaders() {
        return inputMessage.getHeaders();
    }
}