package com.creditease;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.creditease.response.Response;
import com.creditease.spring.resolver.YXHandlerExceptionResolver;
import com.creditease.spring.resolver.YXRequestParamMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class YXWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

    public YXWebMvcConfigurerAdapter() {
        super();
        init();

    }

    /**
     * 默认使用FastJson做JSON解析
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        converters.add(fastConverter);
        super.configureMessageConverters(converters);
    }

    /**添加注解解析器
     *
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        YXRequestParamMethodArgumentResolver argumentResolver = new YXRequestParamMethodArgumentResolver();
        argumentResolvers.add(argumentResolver);
    }

    /**
     * 全局异常统一处理 这块调用的是handlerExceptionResolver方法
     */
    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        super.configureHandlerExceptionResolvers(exceptionResolvers);
        exceptionResolvers.add(new YXHandlerExceptionResolver());
    }

    /**初始化操作
     *
     */
    private void init(){
        Class c = registerReponseCodeClass();
        if(c != null){
            Response.addResultCodeDefinitionClass(c);
        }
    }

    /**
     * 找到响应信息类
     */
    public abstract Class registerReponseCodeClass();
}
