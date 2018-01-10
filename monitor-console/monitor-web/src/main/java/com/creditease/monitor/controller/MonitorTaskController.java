package com.creditease.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.creditease.monitor.constant.MonitorTaskConstant;
import com.creditease.monitor.constant.VerifyUtil;
import com.creditease.monitor.dataclean.DataCleanRuleEntity;
import com.creditease.monitor.exception.MonitorTaskException;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.service.MonitorTaskService;
import com.creditease.monitor.vo.CutExampleVo;
import com.creditease.monitor.vo.EditMonitorTaskVo;
import com.creditease.response.BaseResultCode;
import com.creditease.response.Response;
import com.creditease.spring.annotation.YXRequestParam;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2017/12/12,at 14:54
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@RestController
@RequestMapping("monitorTask")
public class MonitorTaskController {
    private static Logger logger = LoggerFactory.getLogger(MonitorTaskController.class);

    @Autowired
    private MonitorTaskService monitorTaskService;

    //通过taskName模糊搜索
    @RequestMapping("/searchTaskByTaskName")
    public Response searchTaskByTaskName(@YXRequestParam(required = false, errmsg = "服务端根据任务名称模糊搜索发生错误(taskName不能为空)") String taskName,
                                         @YXRequestParam(required = false, errmsg = "服务端根据任务名称模糊搜索发生错误(pageNum不能为空)") Integer pageNum,
                                         @YXRequestParam(required = false, errmsg = "服务端根据任务名称模糊搜索发生错误(pageSize不能为空)") Integer pageSize) {
        logger.info("/searchTaskByTaskName param:taskName:{},pageNum:{},pageSize:{}", taskName, pageNum, pageSize);
        List<MonitorTask> monitorTasksList = monitorTaskService.selectByTaskName(taskName, pageNum, pageSize);

        //分页信息
        return Response.ok(new PageInfo(monitorTasksList));
    }

    //通过taskName查找
    @RequestMapping("/getTaskByTaskName")
    public Response getTaskByTaskName(@YXRequestParam(required = false, errmsg = "服务端根据任务名称查找发生错误(taskName不能为空)") String taskName) {
        logger.info("/getTaskByTaskName param:taskName:{}", taskName);
        MonitorTask monitorTask = monitorTaskService.selectOneByTaskName(taskName);

        //分页信息
        return Response.ok(monitorTask);
    }

    //启动/暂停
    @RequestMapping("/startOrPauseTask")
    public Response startOrPauseTask(@YXRequestParam(required = true, errmsg = "服务端启动/暂停任务状态发生错误") String taskName,
                                     @YXRequestParam(required = true, errmsg = "服务端启动/暂停任务状态发生错误") Byte status) {
        logger.info("/startOrPauseTask param:taskName:{},status={}", taskName, status);
        MonitorTask monitorTask = monitorTaskService.selectOneByTaskName(taskName);
        if (monitorTask == null) {
            logger.info("startOrPauseTask fail taskName={} not exists", taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
        }
        if (status == monitorTask.getStatus()) {
            logger.info("startOrPauseTask 状态一致无需更改 taskName:{},status={}", taskName, status);
            return Response.ok(true);
        }
        monitorTask.setStatus(status);
        try {
            boolean ok = monitorTaskService.startOrPauseTask(monitorTask);
            return Response.ok(ok);
        } catch (MonitorTaskException e) {
            logger.error("startOrPauseTask error param:taskName:{},status={}", taskName, status);
            return Response.fail(e.getErrorCode(), e.getMessage());
        }
    }

    //删除
    @RequestMapping("/deleteTask")
    public Response deleteTask(@YXRequestParam(required = true, errmsg = "服务端删除任务发生错误") String taskName) {
        logger.info("/deleteTask param:taskName:{}", taskName);
        MonitorTask monitorTask = monitorTaskService.selectOneByTaskName(taskName);
        if (monitorTask == null) {
            logger.info("deleteTask fail taskName={} not exists", taskName);
            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
        }
        if (MonitorTaskConstant.MonitorTaskStatus.START == monitorTask.getStatus()) {
            logger.info("deleteTask fail taskName={} is starting", taskName);
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
    public Response dataClean(@YXRequestParam(required = true, errmsg = "测试数据为空") String data,
                              @YXRequestParam(required = true, errmsg = "清洗规则为空") String dataCleanRule) {
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
     *
     * @param monitorTask
     * @return
     */
    @RequestMapping("/addTask")
    public Response addTask(@RequestBody EditMonitorTaskVo monitorTask) {
        logger.info("addTask start taskName={},cutTemplate={},dataSourceLog,dataSourceServerIp={},isMonitorTomcatServer={},tomcatServerHost",
                monitorTask.getTaskName(),
                monitorTask.getCutTemplate(),
                monitorTask.getDataSourceLog(),
                monitorTask.getProjectId(),
                monitorTask.getMachineId());
        Response response = paramVerification(monitorTask);
        if (response != null) {
            return response;
        }
        if (monitorTaskService.isExists(monitorTask.getTaskName())) {
            logger.info("addTask taskName={} has exists", monitorTask.getTaskName());
            return Response.fail(ResponseCode.DATA_SOURCE_HAS_EXISTS);
        }
        boolean ok = monitorTaskService.addTask(monitorTask.getTaskName(),
                monitorTask.getCutTemplate(),
                monitorTask.getDataSourceLog(),
                monitorTask.getProjectId(),
                monitorTask.getMachineId()
               );
        logger.info("addTask end taskName={},result={}", monitorTask.getTaskName(), ok);
        return Response.ok(ok);
    }

    /**
     * 参数验证
     *
     * @param monitorTask
     * @return
     */
    private Response paramVerification(EditMonitorTaskVo monitorTask) {
        if (StringUtils.isBlank(monitorTask.getTaskName())) {
            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "数据源名称不能为空");
        }
        if (StringUtils.isBlank(monitorTask.getCutTemplate())) {
            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "切割模板不能为空");
        }
        if (StringUtils.isBlank(monitorTask.getDataSourceLog())) {
            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "数据源日志不能为空");
        }
        if (null==monitorTask.getProjectId()) {
            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "项目Id不能为空");
        }

        return null;
    }

//    /**
//     * 编辑监控任务
//     *
//     * @param monitorTask
//     * @return
//     */
//    @RequestMapping("/editTask")
//    public Response editTask(@RequestBody EditMonitorTaskVo monitorTask) {
//        logger.info("editTask start taskName={},cutTemplate={},dataSourceLog,dataSourceServerIp={},isMonitorTomcatServer={},tomcatServerHost",
//                monitorTask.getTaskName(),
//                monitorTask.getCutTemplate(),
//                monitorTask.getDataSourceLog(),
//                monitorTask.getDataSourceServerIp(),
//                monitorTask.getIsMonitorTomcatServer(),
//                monitorTask.getTomcatServerHost());
//        Response response = paramVerification(monitorTask);
//        if (response != null) {
//            return response;
//        }
//        MonitorTask monitorTaskDB = monitorTaskService.selectOneByTaskName(monitorTask.getTaskName());
//        if (monitorTaskDB == null) {
//            logger.info("editTask fail taskName={} not exists", monitorTask.getTaskName());
//            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
//        }
//        if (MonitorTaskConstant.MonitorTaskStatus.START == monitorTaskDB.getStatus()) {
//            logger.info("editTask fail taskName={} is starting", monitorTask.getTaskName());
//            return Response.fail(ResponseCode.DATA_SOURCE_IS_STARTING);
//        }
//        boolean ok = monitorTaskService.editTask(monitorTaskDB.getId(), monitorTask.getCutTemplate(), monitorTask.getDataSourceLog(), monitorTask.getDataSourceServerIp(), monitorTask.getIsMonitorTomcatServer(), monitorTask.getTomcatServerHost());
//        logger.info("editTask end taskName={},result={}", monitorTask.getTaskName(), ok);
//        return Response.ok(ok);
//    }
}