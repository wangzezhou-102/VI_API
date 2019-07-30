package com.secusoft.web.core.exception;

/**
 *
 * @Description 业务异常的封装
 * @date 2016年11月12日 下午5:05:10
 */
public class BizException extends RuntimeException {

	private static final long serialVersionUID = 8448020014533545462L;
	
	// 友好提示的code码
	protected int friendlyCode;

	//友好提示
	protected String friendlyMsg;

	//业务异常跳转的页面
	protected String urlPath;

	protected BizException(int friendlyCode, String friendlyMsg, String urlPath) {
		this.setValues(friendlyCode, friendlyMsg, urlPath);
	}

	public BizException(BizExceptionEnum bizExceptionEnum) {
		this.setValues(bizExceptionEnum.getCode(), bizExceptionEnum.getMessage(), bizExceptionEnum.getUrlPath());
	}

	private void setValues(int friendlyCode, String friendlyMsg, String urlPath) {
		this.friendlyCode = friendlyCode;
		this.friendlyMsg = friendlyMsg;
		this.urlPath = urlPath;
	}

	public int getCode() {
		return friendlyCode;
	}

	public void setCode(int code) {
		this.friendlyCode = code;
	}

	public String getMessage() {
		return friendlyMsg;
	}

	public void setMessage(String message) {
		this.friendlyMsg = message;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}
}
