package com.enuos.live.cipher;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.manager.PatternEnum;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @Description 请求响应处理类
 * @Author wangyingjie
 * @Date 2020/9/14
 * @Modified
 */
@ControllerAdvice
public class EncryptAdvice implements ResponseBodyAdvice<Object> {

	/**
	 * 密文开关
	 */
	@Value("${data.is-cipher}")
	private boolean isCipher;

	/** 
	 * @Description: 对加了注解的方法加密 
	 * @Param: [returnType, converterType] 
	 * @Return: boolean 
	 * @Author: wangyingjie
	 * @Date: 2020/9/14 
	 */ 
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.hasMethodAnnotation(Cipher.class);
	}

	/**
	 * @Description: 加密结果
	 * @Param: [body, returnType, selectedContentType, selectedConverterType, request, response]
	 * @Return: java.lang.Object
	 * @Author: wangyingjie
	 * @Date: 2020/9/14
	 */
	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		if (isCipher) {
			if (body instanceof Result) {
				Result result = (Result) body;
				Object data = result.getData();
				Long signature = result.getSignature();

				if (data != null) {
					// JSON时间格式化&禁止循环引用
					result.setData(AESEncoder.encrypt(signature, JSONObject.toJSONStringWithDateFormat(data, PatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern(), SerializerFeature.DisableCircularReferenceDetect)));
				}

				body = result;
			}
		}

		return body;
	}

}