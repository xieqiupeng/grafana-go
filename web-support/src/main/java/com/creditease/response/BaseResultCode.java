package com.creditease.response;

/**
 * 
* @Description: 返回码和提示信息定义。不同模块可继承该类定义自己的返回码
*
* @author gfw
* @version 1.0
 */
public class BaseResultCode {

	/**
	 * @Fields SUCCESS : 成功
	 */
	@ResultMessage("Success!")
	public static final int COMMON_SUCCESS = 0;

	/**
	 * @Fields COMMON_SYSTEM_ERROR : 系统错误
	 */
	@ResultMessage("System error!")
	public static final int COMMON_SYSTEM_ERROR = 1;

	/**
	 * @Fields COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR : 参数解析或校验失败
	 */
	@ResultMessage("Fail to resove&validate parameter!")
	public static final int COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR = 2;
	/**
	 * @Fields LOGIN_SESSION_TIME_OUT : session timeout!
	 */
	@ResultMessage("session timeout!")
	public static final int LOGIN_SESSION_TIME_OUT = 3;
	/**
	 * @Fields COMMON_ILLEGAL_REQUEST : 非法请求
	 */
	@ResultMessage("非法请求!")
	public static final int COMMON_ILLEGAL_REQUEST = 4;
	/**
	 * @Fields COMMON_PREPROGRAM_5 : 预留
	 */
	@ResultMessage("预留!")
	public static final int COMMON_PREPROGRAM_5 = 5;
	/**
	 * @Fields COMMON_PREPROGRAM_6 : 预留
	 */
	@ResultMessage("预留!")
	public static final int COMMON_PREPROGRAM_6 = 6;
	/**
	 * @Fields COMMON_PREPROGRAM_7 : 预留
	 */
	@ResultMessage("预留!")
	public static final int COMMON_PREPROGRAM_7 = 7;

	/**
	 * @Fields COMMON_PREPROGRAM_9 : 预留
	 */
	@ResultMessage("预留!")
	public static final int COMMON_PREPROGRAM_8 = 8;

	/**
	 * @Fields COMMON_PREPROGRAM_9 : 预留
	 */
	@ResultMessage("预留!")
	public static final int COMMON_PREPROGRAM_9 = 9;

	/**
	 * @Fields COMMON_UNKNOWN_ERROR : 未知异常
	 */
	@ResultMessage("Unknown error!")
	public static final int COMMON_UNKNOWN_ERROR = 9999;

}
