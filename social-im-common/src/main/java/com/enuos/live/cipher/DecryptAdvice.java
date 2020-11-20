package com.enuos.live.cipher;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.manager.CharsetEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @Description 请求数据接收处理类，只对[@RequestBody]参数有效
 * @Author wangyingjie
 * @Date 2020/9/14
 * @Modified
 */
@ControllerAdvice
public class DecryptAdvice implements RequestBodyAdvice {

	/**
	 * 密文开关
	 */
	@Value("${data.is-cipher}")
	private boolean isCipher;

	/** 
	 * @Description: 依据注解是否加解密
	 * @Param: [methodParameter, targetType, converterType] 
	 * @Return: boolean 
	 * @Author: wangyingjie
	 * @Date: 2020/9/14 
	 */ 
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return methodParameter.hasMethodAnnotation(Cipher.class);
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
	
	/**
	 * @Description: 封装新的请求
	 * @Param: [inputMessage, parameter, targetType, converterType]
	 * @Return: org.springframework.http.HttpInputMessage
	 * @Author: wangyingjie
	 * @Date: 2020/9/14
	 */
	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		if (isCipher) {
			inputMessage =  new DecryptInputMessage(inputMessage, CharsetEnum.UTF8.getUppercase());
		}

		return inputMessage;
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
}