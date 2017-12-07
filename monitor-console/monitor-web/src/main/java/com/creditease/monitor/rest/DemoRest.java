package com.creditease.monitor.rest;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.StarMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.UserMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.Star;
import com.creditease.monitor.mybatis.sqllite.grafana.po.User;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.response.Response;
import com.creditease.spring.annotation.YXRequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo")
public class DemoRest {

    private static Logger logger = LoggerFactory.getLogger(DemoRest.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StarMapper starMapper;

    @RequestMapping("/demo1")
    public Response demo1(@YXRequestParam(required = true,errmsg = "测试下大小",byteMin = 10,byteMax = 100) byte id){
        logger.info("demo1 start");
        return Response.fail(ResponseCode.errorCode,new Object[]{"参数1","参数2"});
    }

    @RequestMapping("/demo2")
    public Response demo2(){
        logger.info("demo1 start");
        User user = userMapper.selectByPrimaryKey(1);
        return Response.ok(user);
    }


    @RequestMapping("/demo3")
    public Response demo3(){
        logger.info("demo1 start");
        User user = new User();
        user.setVersion(2);
        user.setLogin("root");
        user.setPassword("root");
        user.setEmail("test@creditease.cn");
        user.setOrgId(1);
        user.setIsAdmin(1);
        user.setHelpFlags1(1);
        user.setCreated("2017-11-24 06:00:52");
        user.setUpdated("2017-11-24 06:00:52");
        userMapper.insertSelective(user);
        System.out.println("userId:"+user.getId());
        return Response.ok(user);
    }

    @RequestMapping("/demo4")
    public Response demo4(@YXRequestParam(required = true,errmsg = "测试下大小") Integer id){
        logger.info("demo1 start");
        User user = new User();
        user.setId(id);
        user.setVersion(3);
        userMapper.updateByPrimaryKeySelective(user);
        return Response.ok(user);
    }

    @RequestMapping("/demo5")
    public Response demo5(@YXRequestParam(required = true,errmsg = "测试下大小") Integer id){
        logger.info("demo1 start");
        userMapper.deleteByPrimaryKey(id);
        return Response.ok(null);
    }

    @RequestMapping("/demo6")
    public Response demo6(){
        logger.info("demo1 start");
        Star star = new Star();
        star.setDashboardId(3);
        star.setUserId(1);
        starMapper.insertSelective(star);
        System.out.println("starID::"+star.getId());
        return Response.ok(null);
    }
}
