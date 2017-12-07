
package com.creditease.exception;

/**
 * 
* @Description: 非法请求异常
 */
public class IllegalRequestException extends RuntimeException{

	/**
	* @Fields serialVersionUID : TODO
	*/
	private static final long serialVersionUID = -7502907969661712127L;

	public IllegalRequestException() {
		super();	
	}

	public IllegalRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalRequestException(String message, Throwable cause) {
		super(message, cause);	
	}

	public IllegalRequestException(String message) {
		super(message);
	}

	public IllegalRequestException(Throwable cause) {
		super(cause);	
	}
	
	

}
