
package com.creditease.request;

import com.creditease.exception.IllegalRequestException;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;


public abstract class XssMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest{

	public static final String BAD_SQL_KEY =XssMultipartHttpServletRequest.class.getName()+"_badSql";
	private String[] badSql = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|master|truncate|declare|like".split("\\|");
	
	@Override
	public String getHeader(String name) {
		String value = super.getHeader(name);
		if (value == null)
	        return null;
	    if("Accept".equals(name)){
	        return value;
	    }
	    return cleanXSS(value);
	}
	/**
	 * 
	* @Description: 获取原始的参数
	 */
	protected abstract String getParameterWithXss(String name);
	
	@Override
	public String getParameter(String name) {
		String value = getParameterWithXss(name);
		if(value != null){
			return cleanXSS(value);
		}
		return value;
	}
	/**
	 * 
	* @Description: 获取原始的参数
	 */
	protected abstract String[] getParameterValuesWithXss(String name);
	
	@Override
	public String[] getParameterValues(String name) {
		String[] values = getParameterValuesWithXss(name);
		if (values != null) {
			for(int i = 0; i<values.length;i++){
				values[i] = cleanXSS(values[i]);
			}
		}
		return values;
	}
	protected XssMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
		Object badSqlObj = request.getAttribute(BAD_SQL_KEY);
		if(badSqlObj != null){
			setBadSqlStr(badSqlObj.toString());
		}
	}
	protected String cleanXSS(String value) {
        //You'll need to remove the spaces from the html entities below
        value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
        value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
        value = value.replaceAll("'", "& #39;");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");
        sqlValidate(value);
        return value;
	}
	protected void sqlValidate(String str) {   
	    str = str.toLowerCase();//统一转为小写
	    for (int i = 0; i < badSql.length; i++) {   
	        if (str.indexOf(badSql[i]) >= 0) {
	        	throw new IllegalRequestException("提交的参数中存在非法字符,请检查后重试!(string=>"+badSql[i]+")");
	        }   
	    }   
	}
	public void setBadSqlStr(String badSqlStr) {
		if(badSqlStr != null && "".equals(badSqlStr.trim())){
			badSql = badSqlStr.split("\\|");
		}
	}
}
