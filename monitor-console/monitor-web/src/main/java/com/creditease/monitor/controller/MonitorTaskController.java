package com.creditease.monitor.controller;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorTaskMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.StarMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.UserMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorTaskExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.mybatis.sqllite.grafana.po.Star;
import com.creditease.monitor.mybatis.sqllite.grafana.po.User;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.service.MonitorTaskService;
import com.creditease.response.Response;
import com.creditease.spring.annotation.YXRequestParam;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2017/12/12,at 14:54
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@RestController
@RequestMapping("monitortask")
public class MonitorTaskController {
    private static Logger logger = LoggerFactory.getLogger(MonitorTaskController.class);

    @Autowired
    private MonitorTaskService monitorTaskService;

    //通过taskName搜索
    @RequestMapping("/searchtaskbytaskname")
    public Response searchTaskByTaskName(@YXRequestParam(required = false,errmsg = "服务端根据任务名称搜索发生错误(taskName不能为空)") String taskName,
                                         @YXRequestParam(required = false,errmsg = "服务端根据任务名称搜索发生错误(pageNum不能为空)") Integer pageNum,
                                         @YXRequestParam(required = false,errmsg = "服务端根据任务名称搜索发生错误(pageSize不能为空)") Integer pageSize){
        logger.info("/searchtaskbytaskname param:taskName:{},pageNum:{},pageSize:{}",taskName,pageNum,pageSize);
        List<MonitorTask> monitorTasksList = monitorTaskService.selectByTaskName(taskName,pageNum,pageSize);
        //分页信息
        return Response.ok(new PageInfo(monitorTasksList));
    }

    //启动/暂停
    @RequestMapping("/startorpausetask")
    public Response startOrPauseTask(@YXRequestParam(required = true,errmsg = "服务端启动/暂停任务状态发生错误") Integer taskId){
        logger.info("/startorpausetask param:taskId:{}",taskId);
        monitorTaskService.startOrPauseTask(taskId);
        return Response.ok(null);
    }

    //删除
    @RequestMapping("/deletetask")
    public Response deleteTask(@YXRequestParam(required = true,errmsg = "服务端删除任务发生错误") Integer taskId){
        logger.info("/deletetask param:taskId:{}",taskId);
        monitorTaskService.deleteTask(taskId);
        return Response.ok(null);
    }

//
//    //通过taskId搜索
//    @RequestMapping("/searchtaskbytaskid")
//    public Response searchTaskByTaskId(){
//        logger.info("demo1 start");
//        User user = userMapper.selectByPrimaryKey(1);
//        return Response.ok(user);
//    }
//
    //新增
    @RequestMapping("/addtask")
    public Response addTask(@YXRequestParam(required = true,errmsg = "服务端新增任务错误") @RequestBody MonitorTask monitorTask){
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
        //userMapper.insertSelective(user);
        System.out.println("monitorTask:"+monitorTask);
        System.out.println("monitorTask:"+monitorTask.getTaskName()+" "+monitorTask.getDataSourceIp());
        return Response.ok(user);
    }

    //新增
    @RequestMapping("/addtask1")
    public Response addTask1(@YXRequestParam(required = true,errmsg = "服务端新增任务错误") String taskName){
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

        return Response.ok(user);
    }


//    //编辑
//    @RequestMapping("/edittask")
//    public Response editTask(@YXRequestParam(required = true,errmsg = "服务端编辑任务发生错误") Integer id){
//        logger.info("demo1 start");
//        User user = new User();
//        user.setId(id);
//        user.setVersion(3);
//        userMapper.updateByPrimaryKeySelective(user);
//        return Response.ok(user);
//    }
//

//
//    @RequestMapping("/demo6")
//    public Response demo6(){
//        logger.info("demo1 start");
//        Star star = new Star();
//        star.setDashboardId(3);
//        star.setUserId(1);
//        starMapper.insertSelective(star);
//        System.out.println("starID::"+star.getId());
//        return Response.ok(null);
//    }
//


    public static void main(String[] args) {

        System.out.println(System.currentTimeMillis());
    }

}
