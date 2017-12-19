package com.creditease.interceptors;

import com.alibaba.fastjson.JSON;
import com.creditease.response.BaseResultCode;
import com.creditease.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class SecurityInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityInterceptor.class);

    private SecurityVerify securityVerify;

    public SecurityInterceptor(SecurityVerify securityVerify) {
        super();
        this.securityVerify = securityVerify;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean pass = securityVerify.pass(request);
        if(!pass){
            outJson(response, Response.fail(BaseResultCode.LOGIN_SESSION_TIME_OUT));
            return false;
        }
        return true;
    }

    private void outJson(HttpServletResponse response, Object JSONobject){
        PrintWriter out = null;
        try{
            out = response.getWriter();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            response.setHeader("pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            out.write(JSON.toJSONString(JSONobject, true) );
            return;
        } catch (Exception e) {
            log.error("",e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 安全检验
     */
    public interface SecurityVerify{
        /**
         * 是否通过
         * @param request
         * @return
         */
        boolean pass(HttpServletRequest request);

    }
}
