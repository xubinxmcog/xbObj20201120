package com.enuos.live.error;

/**
 * @Description 业务异常的封装
 * 
 * @date 2016年11月12日 下午5:05:10
 */
public class BussinessException extends RuntimeException{

	//友好提示的code码
	private int friendlyCode;
	
	//友好提示
	private String friendlyMsg;

	public BussinessException(String msg) {
		super();
		this.friendlyCode = 500;
		this.friendlyMsg = msg;
	}

	public BussinessException(int code, String msg) {
		super();
		this.friendlyCode = code;
		this.friendlyMsg = msg;
	}

	
	public BussinessException(String errorCode, String message) {
		super();
		this.friendlyCode = Integer.parseInt(errorCode);
		this.friendlyMsg = message;
	}

	public int getCode() {
		return friendlyCode;
	}

	public void setCode(int code) {
		this.friendlyCode = code;
	}

	@Override
	public String getMessage() {
		return friendlyMsg;
	}

	public void setMessage(String message) {
		this.friendlyMsg = message;
	}
	
}
