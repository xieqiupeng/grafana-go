package com.creditease.monitor;

import com.creditease.YXWebMvcConfigurerAdapter;
import com.creditease.interceptors.AccessInterceptor;
import com.creditease.interceptors.SecurityInterceptor;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.SessionExMapper;
import com.creditease.monitor.response.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Configuration
public class InitWebMvcConfigurerAdapter extends YXWebMvcConfigurerAdapter {
    @Override
    public Class registerReponseCodeClass() {
        return ResponseCode.class;
    }

    @Autowired
    private SessionExMapper sessionExMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AccessInterceptor());
        registry.addInterceptor(new SecurityInterceptor(new SecurityInterceptor.SecurityVerify() {
            @Override
            public boolean pass(HttpServletRequest request) {
                //String token = request.getHeader("grafana_sess");
                String token="";
                Cookie[] cookies = request.getCookies();
                if(null==cookies){
                    return false;
                }
                for(int i=0;i<cookies.length;i++){
                    if("grafana_sess".equals(cookies[i].getName())){
                        token=cookies[i].getValue();
                        break;
                    }
                }

                if(StringUtils.isBlank(token)){
                    return false;
                }
                Integer expiry = sessionExMapper.selectExpiryByKey(token);
                if(expiry == null){
                    return false;
                }
                if(System.currentTimeMillis()/1000 >= expiry){
                    return false;
                }
                return true;
            }
        })).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
