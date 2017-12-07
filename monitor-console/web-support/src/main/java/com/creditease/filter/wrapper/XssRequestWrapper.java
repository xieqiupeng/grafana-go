package com.creditease.filter.wrapper;


import com.creditease.exception.IllegalRequestException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * HTML转义和SQL关键字过滤（默认只针对Mysql进行关键字过滤）
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {
	private static final String[] DEFAULT_BAD_SQL = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|master|truncate|declare|like".split("\\|");   
    private String[] badSql = null;
    
    public XssRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = cleanXSS(values[i]);
        }
        return encodedValues;
    }

    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        if (value == null) {
            return null;
        }
        return cleanXSS(value);
    }

    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null)
            return null;
        if("Accept".equals(name)){
        	return value;
        }
        return cleanXSS(value);
    }

    private String cleanXSS(String value) {
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
        if(badSql==null){
        	badSql = DEFAULT_BAD_SQL;
        }
        for (int i = 0; i < badSql.length; i++) {   
            if (str.indexOf(badSql[i]) >= 0) {
            	throw new IllegalRequestException("提交的参数中存在非法字符,请检查后重试!(string=>"+badSql[i]+")");
            }   
        }   
    }

	public String[] getBadSql() {
		return badSql;
	}

	public void setBadSql(String[] badSql) {
		this.badSql = badSql;
	}   

}
