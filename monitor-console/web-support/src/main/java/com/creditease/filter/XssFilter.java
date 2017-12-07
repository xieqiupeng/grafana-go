package com.creditease.filter;



import com.creditease.filter.wrapper.XssRequestWrapper;
import com.creditease.request.XssMultipartHttpServletRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * 
* @Description: 对XSS攻击和SQL注入进行过滤
 */
public class XssFilter implements Filter {

	private String[] BAD_SQL = null;
	FilterConfig filterConfig = null;
	private static String MULTIPART = "multipart/";
	private String badsql = null;

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		String sql = filterConfig.getInitParameter("sql");
		this.badsql = sql;
		if (null != null && !"".equals(sql.trim())) {
			BAD_SQL = sql.split("\\|");
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(!isMultipartContent((HttpServletRequest) request)){
			XssRequestWrapper wrapper = new XssRequestWrapper((HttpServletRequest) request);
			if (BAD_SQL != null) {
				wrapper.setBadSql(BAD_SQL);
			}
			chain.doFilter(wrapper, response);
		}else{
			request.setAttribute(XssMultipartHttpServletRequest.BAD_SQL_KEY, badsql);
			chain.doFilter(request, response);
		}
	}

	public void destroy() {
		this.filterConfig = null;
	}
	
	private boolean isMultipartContent(
            HttpServletRequest request) {
        if (!"post".equals(request.getMethod().toLowerCase())) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase().startsWith(MULTIPART)) {
            return true;
        }
        return false;
    }
}
