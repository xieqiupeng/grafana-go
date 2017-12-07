package com.creditease.monitor;

import com.creditease.YXWebMvcConfigurerAdapter;
import com.creditease.monitor.response.ResponseCode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application extends YXWebMvcConfigurerAdapter {
    @Override
    public Class registerReponseCodeClass() {
        return ResponseCode.class;
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
