package com.creditease.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.creditease.monitor.dataclean.DataCleanRuleEntity;
import com.creditease.monitor.constant.MonitorTaskConstant;
import com.creditease.monitor.exception.MonitorTaskException;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.service.MonitorTaskService;
import com.creditease.monitor.vo.CutExampleVo;
import com.creditease.response.BaseResultCode;
import com.creditease.response.Response;
import com.creditease.spring.annotation.YXRequestParam;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
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
    public Response searchTaskByTaskName(@YXRequestParam(required = false, errmsg = "服务端根据任务名称搜索发生错误(taskName不能为空)") String taskName,
                                         @YXRequestParam(required = false, errmsg = "服务端根据任务名称搜索发生错误(pageNum不能为空)") Integer pageNum,
                                         @YXRequestParam(required = false, errmsg = "服务端根据任务名称搜索发生错误(pageSize不能为空)") Integer pageSize) {
        logger.info("/searchtaskbytaskname param:taskName:{},pageNum:{},pageSize:{}", taskName, pageNum, pageSize);
        List<MonitorTask> monitorTasksList = monitorTaskService.selectByTaskName(taskName, pageNum, pageSize);
        //分页信息
        return Response.ok(new PageInfo(monitorTasksList));
    }

    //启动/暂停
    @RequestMapping("/startOrPauseTask")
    public Response startOrPauseTask(@YXRequestParam(required = true, errmsg = "服务端启动/暂停任务状态发生错误") String taskName,
                                     @YXRequestParam(required = true, errmsg = "服务端启动/暂停任务状态发生错误") Byte status) {
        logger.info("/startOrPauseTask param:taskName:{},status={}",taskName,status);
        MonitorTask monitorTask = monitorTaskService.selectOneByTaskName(taskName);
        if(monitorTask == null){
            logger.info("startOrPauseTask fail taskName={} not exists",taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
        }
        if(status == monitorTask.getStatus()){
            logger.info("startOrPauseTask 状态一致无需更改 taskName:{},status={}",taskName,status);
            return Response.ok(true);
        }
        monitorTask.setStatus(status);
        try {
            boolean ok = monitorTaskService.startOrPauseTask(monitorTask);
            return Response.ok(ok);
        }catch (MonitorTaskException e){
            logger.error("startOrPauseTask error param:taskName:{},status={}",taskName,status);
            return Response.fail(e.getErrorCode(),e.getMessage());
        }
    }

    //删除
    @RequestMapping("/deleteTask")
    public Response deleteTask(@YXRequestParam(required = true, errmsg = "服务端删除任务发生错误") String taskName) {
        logger.info("/deleteTask param:taskName:{}", taskName);
        MonitorTask monitorTask = monitorTaskService.selectOneByTaskName(taskName);
        if(monitorTask == null){
            logger.info("edittask fail taskName={} not exists",taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
        }
        if(MonitorTaskConstant.MonitorTaskStatus.START == monitorTask.getStatus()){
            logger.info("edittask fail taskName={} is starting",taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_IS_STARTING);
        }
        boolean ok = monitorTaskService.deleteTask(monitorTask.getId());
        return Response.ok(ok);
    }

    /**
     * 数据清洗
     *
     * @param data          数据
     * @param dataCleanRule 清洗规则
     * @return
     */
    @RequestMapping("/dataClean")
    public Response dataClean(@YXRequestParam(required = true, errmsg = "数据为空") String data,
                              @YXRequestParam(required = true, errmsg = "数据为空") String dataCleanRule) {
        logger.info("数据清洗开始 data={},dataCleanRule={}", data, dataCleanRule);
        try {
            String str = data.trim();
            List<String> monitorDates = Arrays.asList(str.split("\\r\\n"));
            DataCleanRuleEntity dataCleanRuleEntity = JSON.parseObject(dataCleanRule, DataCleanRuleEntity.class);
            List<CutExampleVo> vos = monitorTaskService.dataClean(monitorDates, dataCleanRuleEntity);
            logger.info("数据清洗完成 data={},dataCleanRule={}", data, dataCleanRule);
            return Response.ok(vos);
        } catch (Exception e) {
            return Response.fail(BaseResultCode.COMMON_SYSTEM_ERROR, "数据清洗异常");
        }
    }

    /**
     * 新增监控任务
     * @param taskName
     * @param cutTemplate
     * @param dataSourceLog
     * @param dataSourceServerIp
     * @param isMonitorTomcatServer
     * @param tomcatServerHost
     * @return
     */
    @RequestMapping("/addTask")
    public Response addTask(@YXRequestParam(required = true, errmsg = "数据源名称不能为空") String taskName,
                            @YXRequestParam(required = true, errmsg = "切割模板不能为空") String cutTemplate,
                            @YXRequestParam(required = true, errmsg = "日志文件路径不能为空") String dataSourceLog,
                            @YXRequestParam(required = true, errmsg = "服务器host不能为空") String dataSourceServerIp,
                            @YXRequestParam(required = true, errmsg = "") Byte isMonitorTomcatServer,
                            @YXRequestParam(required = false) String tomcatServerHost) {
        logger.info("addtask start taskName={},cutTemplate={},dataSourceLog,dataSourceServerIp={},isMonitorTomcatServer={},tomcatServerHost",
                taskName, cutTemplate,dataSourceLog, dataSourceServerIp, isMonitorTomcatServer, tomcatServerHost);
        if(monitorTaskService.isExists(taskName)){
            logger.info("addtask taskName={} has exists",taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_HAS_EXISTS);
        }
        boolean ok = monitorTaskService.addTask(taskName,cutTemplate,dataSourceLog,dataSourceServerIp,isMonitorTomcatServer,tomcatServerHost);
        logger.info("addtask end taskName={},result={}",taskName,ok);
        return Response.ok(ok);
    }

    /**
     * 编辑监控任务
     * @param taskName
     * @param cutTemplate
     * @param dataSourceLog
     * @param dataSourceServerIp
     * @param isMonitorTomcatServer
     * @param tomcatServerHost
     * @return
     */
    @RequestMapping("/editTask")
    public Response editTask(@YXRequestParam(required = true, errmsg = "数据源名称不能为空") String taskName,
                             @YXRequestParam(required = true, errmsg = "切割模板不能为空") String cutTemplate,
                             @YXRequestParam(required = true, errmsg = "日志文件路径不能为空") String dataSourceLog,
                             @YXRequestParam(required = true, errmsg = "服务器host不能为空") String dataSourceServerIp,
                             @YXRequestParam(required = true, errmsg = "") Byte isMonitorTomcatServer,
                             @YXRequestParam(required = false) String tomcatServerHost) {
        logger.info("edittask start taskName={},cutTemplate={},dataSourceLog,dataSourceServerIp={},isMonitorTomcatServer={},tomcatServerHost",
                taskName, cutTemplate,dataSourceLog, dataSourceServerIp, isMonitorTomcatServer, tomcatServerHost);
        MonitorTask monitorTask = monitorTaskService.selectOneByTaskName(taskName);
        if(monitorTask == null){
            logger.info("edittask fail taskName={} not exists",taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
        }
        if(MonitorTaskConstant.MonitorTaskStatus.START == monitorTask.getStatus()){
            logger.info("edittask fail taskName={} is starting",taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_IS_STARTING);
        }
        boolean ok = monitorTaskService.editTask(monitorTask.getId(),cutTemplate,dataSourceLog,dataSourceServerIp,isMonitorTomcatServer,tomcatServerHost);
        logger.info("edittask end taskName={},result={}",taskName,ok);
        return Response.ok(ok);
    }

}
