
package com.creditease.response;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * 
* @Description:  专用响应对象
 */
public class Response extends AbstractApiBaseResponse implements Serializable{
	private final static Logger log = LoggerFactory.getLogger(Response.class);
	/**
	* @Fields serialVersionUID : TODO
	*/
	private static final long serialVersionUID = -859063786999085093L;
	/**提示信息**/
	private String resultMsg = getResultMsg(BaseResultCode.COMMON_SUCCESS);
	/**错误码*/
	private int resultCode =  BaseResultCode.COMMON_SUCCESS;
	/**相应实体**/
	private Object data;
	
	private Response(){
		
	}
	/**静态工厂：生成一个成功的Response**/
	public static Response ok(Object data) {
		Response response = new Response();
		response.setData(data);
		response.setResultCode(BaseResultCode.COMMON_SUCCESS);
		String rm = getResultMsg(BaseResultCode.COMMON_SUCCESS);
		rm = null != rm ? rm : "Success!";
		response.setResultMsg(rm);
		return response;
	}
	
	public static Response fail(Integer errorCode) {
		return fail(errorCode, null, new Object[0]);
	}
	public static Response fail(Integer errorCode,String errorMsg) {
		return fail(errorCode, errorMsg, new Object[0]);
	}
	public static Response fail(Integer errorCode,Object... formatErrorMsgArgs) {
		return fail(errorCode, null, formatErrorMsgArgs);
	}

	public static Response fail(Integer errorCode,String errorMsg,Object... formatErrorMsgArgs) {
		Response response = new Response();
		//确定错误码
		if (null == errorCode) {
			errorCode = BaseResultCode.COMMON_SYSTEM_ERROR;
		}
		response.setResultCode(errorCode);
		String errMsg = null;
		try {
			errMsg = getResultMsg(errorCode);
			if (null != errMsg && !"".equals(errMsg.trim())) {
				if (null != formatErrorMsgArgs && 0 < formatErrorMsgArgs.length) {
					try {
						errMsg = MessageFormat.format(errMsg, formatErrorMsgArgs);
					} catch (Exception e) {
						log.error("Fail to format result msg! errorCode:" + errorCode + ",formatErrorMsgArgs:"
								+ JSON.toJSONString(formatErrorMsgArgs), e);
					}
				}

				if (StringUtils.isNotBlank(errorMsg)) {
					errMsg += (" " + errorMsg);
				}
			} else {
				errMsg = errorMsg;
			}
			response.setResultMsg(errMsg);
		} catch (Exception e) {
			log.error("Fail to get result msg! errorCode:" + errorCode + ",formatErrorMsgArgs:" + JSON.toJSONString(formatErrorMsgArgs), e);
		}
		return response;
	}
	public String getResultMsg() {
		return resultMsg;
	}

	public Object getData() {
		return data;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
