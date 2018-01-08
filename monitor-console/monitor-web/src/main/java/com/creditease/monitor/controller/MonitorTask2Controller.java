package com.creditease.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.creditease.monitor.constant.MonitorTaskConstant;
import com.creditease.monitor.constant.VerifyUtil;
import com.creditease.monitor.dataclean.DataCleanRuleEntity;
import com.creditease.monitor.exception.MonitorTaskException;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask2;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.service.MonitorTask2Service;
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
@RequestMapping("monitorTask2")
public class MonitorTask2Controller {
    private static Logger logger = LoggerFactory.getLogger(MonitorTask2Controller.class);

    @Autowired
    private MonitorTask2Service monitorTaskService;

    //通过taskName模糊搜索
    @RequestMapping("/searchTaskByTaskName")
    public Response searchTaskByTaskName(@YXRequestParam(required = false, errmsg = "服务端根据任务名称模糊搜索发生错误(taskName不能为空)") String taskName,
                                         @YXRequestParam(required = false, errmsg = "服务端根据任务名称模糊搜索发生错误(pageNum不能为空)") Integer pageNum,
                                         @YXRequestParam(required = false, errmsg = "服务端根据任务名称模糊搜索发生错误(pageSize不能为空)") Integer pageSize) {
        logger.info("/searchTaskByTaskName param:taskName:{},pageNum:{},pageSize:{}", taskName, pageNum, pageSize);
        List<MonitorTask2> monitorTasksList = monitorTaskService.selectByTaskName(taskName, pageNum, pageSize);

        //分页信息
        return Response.ok(new PageInfo(monitorTasksList));
    }

    //通过taskName查找
    @RequestMapping("/getTaskByTaskName")
    public Response getTaskByTaskName(@YXRequestParam(required = false, errmsg = "服务端根据任务名称查找发生错误(taskName不能为空)") String taskName) {
        logger.info("/getTaskByTaskName param:taskName:{}", taskName);
        MonitorTask2 monitorTask = monitorTaskService.selectOneByTaskName(taskName);

        //分页信息
        return Response.ok(monitorTask);
    }

//    //启动/暂停
//    @RequestMapping("/startOrPauseTask")
//    public Response startOrPauseTask(@YXRequestParam(required = true, errmsg = "服务端启动/暂停任务状态发生错误") String taskName,
//                                     @YXRequestParam(required = true, errmsg = "服务端启动/暂停任务状态发生错误") Byte status) {
//        logger.info("/startOrPauseTask param:taskName:{},status={}", taskName, status);
//        MonitorTask2 monitorTask = monitorTaskService.selectOneByTaskName(taskName);
//        if (monitorTask == null) {
//            logger.info("startOrPauseTask fail taskName={} not exists", taskName);
//            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
//        }
//        if (status == monitorTask.getStatus()) {
//            logger.info("startOrPauseTask 状态一致无需更改 taskName:{},status={}", taskName, status);
//            return Response.ok(true);
//        }
//        monitorTask.setStatus(status);
//        try {
//            boolean ok = monitorTaskService.startOrPauseTask(monitorTask);
//            return Response.ok(ok);
//        } catch (MonitorTaskException e) {
//            logger.error("startOrPauseTask error param:taskName:{},status={}", taskName, status);
//            return Response.fail(e.getErrorCode(), e.getMessage());
//        }
//    }

    //删除
    @RequestMapping("/deleteTask")
    public Response deleteTask(@YXRequestParam(required = true, errmsg = "服务端删除任务发生错误") String taskName) {
        logger.info("/deleteTask param:taskName:{}", taskName);
        MonitorTask2 monitorTask = monitorTaskService.selectOneByTaskName(taskName);
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
//    @RequestMapping("/dataClean")
//    public Response dataClean(@YXRequestParam(required = true, errmsg = "测试数据为空") String data,
//                              @YXRequestParam(required = true, errmsg = "清洗规则为空") String dataCleanRule) {
//        logger.info("数据清洗开始 data={},dataCleanRule={}", data, dataCleanRule);
//        try {
//            String str = data.trim();
//            List<String> monitorDates = Arrays.asList(str.split("\\r\\n"));
//            DataCleanRuleEntity dataCleanRuleEntity = JSON.parseObject(dataCleanRule, DataCleanRuleEntity.class);
//            List<CutExampleVo> vos = monitorTaskService.dataClean(monitorDates, dataCleanRuleEntity);
//            logger.info("数据清洗完成 data={},dataCleanRule={}", data, dataCleanRule);
//            return Response.ok(vos);
//        } catch (Exception e) {
//            return Response.fail(BaseResultCode.COMMON_SYSTEM_ERROR, "数据清洗异常");
//        }
//    }

//    /**
//     * 新增监控任务
//     *
//     * @param monitorTask
//     * @return
//     */
//    @RequestMapping("/addTask")
//    public Response addTask(@RequestBody EditMonitorTaskVo monitorTask) {
//        logger.info("addTask start taskName={},cutTemplate={},dataSourceLog,dataSourceServerIp={},isMonitorTomcatServer={},tomcatServerHost",
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
//        if (monitorTaskService.isExists(monitorTask.getTaskName())) {
//            logger.info("addTask taskName={} has exists", monitorTask.getTaskName());
//            return Response.fail(ResponseCode.DATA_SOURCE_HAS_EXISTS);
//        }
//        boolean ok = monitorTaskService.addTask(monitorTask.getTaskName(),
//                monitorTask.getCutTemplate(),
//                monitorTask.getDataSourceLog(),
//                monitorTask.getDataSourceServerIp(),
//                monitorTask.getIsMonitorTomcatServer(),
//                monitorTask.getTomcatServerHost());
//        logger.info("addTask end taskName={},result={}", monitorTask.getTaskName(), ok);
//        return Response.ok(ok);
//    }

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
        if (StringUtils.isBlank(monitorTask.getDataSourceServerIp())) {
            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "服务器IP不能为空");
        }
        String dataSourceServerIp = monitorTask.getDataSourceServerIp();
        String[] dataSourceServerIpArray = dataSourceServerIp.split(MonitorTaskConstant.comma);
        Set<String> ips = new HashSet<>();
        for (String ip : dataSourceServerIpArray) {
            if (!VerifyUtil.isIP(ip)) {
                logger.info("存在无效IP地址,ip={}", ip);
                return Response.fail(ResponseCode.INVALID_IP, new Object[]{ip});
            }
            if (ips.contains(ip)) {
                logger.info("addTask 重复IP={}", ip);
                return Response.fail(ResponseCode.IP_REPEAT, new Object[]{ip});
            }
            ips.add(ip);
        }

        if (MonitorTaskConstant.MonitorTomcatServer.YES == monitorTask.getIsMonitorTomcatServer()
                && StringUtils.isNotBlank(monitorTask.getTomcatServerHost())) {
            Map<String, List<Integer>> map = new HashMap<>();
            String tomcatServerHostStr = monitorTask.getTomcatServerHost();
            String[] tomcatServerHostArray = tomcatServerHostStr.split(MonitorTaskConstant.comma);
            for (String tomcatServerHost : tomcatServerHostArray) {
                String[] ipPort = tomcatServerHost.split(MonitorTaskConstant.colon);
                if (ipPort.length < 1) {
                    logger.info("addTask fail 存在无效的tomcat服务地址 {}", ipPort);
                    return Response.fail(ResponseCode.INVALID_TOMCAT_ADDRESS, new Object[]{tomcatServerHost});
                }
                String ip = ipPort[0];
                if (!ips.contains(ip)) {
                    logger.info("监控服务地址和服务器地址IP不一致:{}", tomcatServerHost);
                    return Response.fail(ResponseCode.IP_HAS_DIFFER, new Object[]{tomcatServerHost});
                }
                String portStr = ipPort[1];
                int port = 0;
                try {
                    port = Integer.parseInt(portStr);
                } catch (Exception e) {
                    logger.info("无效端口 port={}", portStr);
                }
                if (port <= 0 || port > 65535) {
                    logger.info("存在无效的端口{}", portStr);
                    return Response.fail(ResponseCode.INVALID_PORT, new Object[]{portStr});

                }
                List<Integer> list = map.get(ip);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(ip, list);
                }
                if (list.contains(port)) {
                    logger.info("存在重复的端口{}", portStr);
                    return Response.fail(ResponseCode.TOMCAT_ADDRESS_REPEAT, new Object[]{port});
                }
                list.add(port);
            }
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