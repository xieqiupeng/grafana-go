package com.creditease.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * 字符集过滤
 */
public class EncodingFilter implements Filter {   
  
    private String encoding = null;   
  
    public void destroy() {   
         encoding = null;   
     }   
  
    public void doFilter(ServletRequest request, ServletResponse response,   
             FilterChain chain) throws IOException, ServletException {   
        String encoding = getEncoding();   
        if (encoding == null){   
             encoding = "UTF-8";   
         }   
         request.setCharacterEncoding(encoding);// 在请求里设置上指定的编码
         response.setCharacterEncoding(encoding);
         chain.doFilter(request, response);   
     }   
  
    public void init(FilterConfig filterConfig) throws ServletException {   
        this.encoding = filterConfig.getInitParameter("encoding");   
     }   
  
    private String getEncoding() {   
        return this.encoding;   
     }   
  
}  