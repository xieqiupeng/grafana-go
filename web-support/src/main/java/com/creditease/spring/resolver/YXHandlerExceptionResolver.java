package com.creditease.spring.resolver;

import com.alibaba.fastjson.JSON;
import com.creditease.exception.IllegalRequestException;
import com.creditease.response.BaseResultCode;
import com.creditease.response.Response;
import com.creditease.spring.exception.YXRequestParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 
* @Description: 同一异常处理
*
 */
public class YXHandlerExceptionResolver implements HandlerExceptionResolver {
	
	private Logger log = LoggerFactory.getLogger(YXHandlerExceptionResolver.class);

	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object arg2, Exception ex) {
		Response res = null;
		if(ex instanceof IllegalRequestException){
			res = Response.fail(BaseResultCode.COMMON_ILLEGAL_REQUEST);
		}else if(ex instanceof YXRequestParamException){
			String disMessage = ((YXRequestParamException)ex).getDisMessage();
			res = Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR,disMessage);
		}else{
			res = Response.fail(BaseResultCode.COMMON_SYSTEM_ERROR);
		}
		output(response, res);
		return null;
	}
	private void output(HttpServletResponse response, Object JSONobject) {
		try {
			output(response, JSON.toJSONString(JSONobject, true));
		} catch (Exception e) {
			log.error("",e);
		}
	}
	private void output(HttpServletResponse response, String str) {
		PrintWriter out = null;
		try {
			response.setContentType("application/json; charset=UTF-8");
			out = response.getWriter();
			out.write(str);
		} catch (Exception e) {
			log.error("",e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}