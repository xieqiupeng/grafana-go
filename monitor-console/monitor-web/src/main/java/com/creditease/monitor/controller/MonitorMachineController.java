package com.creditease.monitor.controller;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorMachine;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.service.MonitorApplicationService;
import com.creditease.monitor.service.MonitorMachineService;
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
 * @Date: created on 2018/01/04,at 14:05
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@RestController
@RequestMapping("monitorMachine")
public class MonitorMachineController {
    private static Logger logger = LoggerFactory.getLogger(MonitorMachineController.class);

    @Autowired
    private MonitorMachineService monitorMachineService;

    @Autowired
    private MonitorApplicationService monitorApplicationService;

    @Autowired
    private MonitorTaskService monitorTaskService;

    //通过MachineName模糊搜索
    @RequestMapping("/searchMachineByMachineName")
    public Response searchMachineByMachineName(@YXRequestParam(required = false, errmsg = "服务端根据机器名称模糊搜索发生错误(machineName不能为空)") String machineName,
                                               @YXRequestParam(required = false, errmsg = "服务端根据机器名称模糊搜索发生错误(projectId)") Integer projectId,
                                         @YXRequestParam(required = false, errmsg = "服务端根据机器名称模糊搜索发生错误(pageNum不能为空)") Integer pageNum,
                                         @YXRequestParam(required = false, errmsg = "服务端根据机器名称模糊搜索发生错误(pageSize不能为空)") Integer pageSize) {
        logger.info("/searchMachineByMachineName param:machineName:{},pageNum:{},pageSize:{}", machineName, pageNum, pageSize);
        List<MonitorMachine> monitorMachinesList = monitorMachineService.selectByMachineNameAndProjectId(machineName,projectId, pageNum, pageSize);

        //分页信息
        return Response.ok(new PageInfo(monitorMachinesList));
    }

    //通过machineName查找
    @RequestMapping("/getMachineByMachineName")
    public Response getMachineByMachineName(@YXRequestParam(required = false, errmsg = "服务端根据机器名称查找发生错误(machineName不能为空)") String machineName) {
        logger.info("/getMachineByMachineName param:machineName:{}", machineName);
        MonitorMachine monitorMachine = monitorMachineService.selectOneByMachineName(machineName);

        //分页信息
        return Response.ok(monitorMachine);
    }
    

    //删除
    @RequestMapping("/deleteMachine")
    public Response deleteMachine(@YXRequestParam(required = true, errmsg = "服务端删除机器发生错误,id不能为空") Integer id) {
        logger.info("/deleteMachine id:{}", id);
        MonitorMachine monitorMachine = monitorMachineService.selectOneByMachineId(id);
        if (monitorMachine == null) {
            logger.info("deleteMachine fail id={} not exists", id);
            return Response.fail(ResponseCode.MACHINE_NOT_EXISTS);
        }
        if (monitorApplicationService.referMachine(id)) {
            logger.info("deleteMachine failed,some application is referring machine id={}  ",id);
            return Response.fail(ResponseCode.MACHINE_IS_REFERRED_BY_APPLICATION);
        }

        if(monitorTaskService.referMachine(id)){
            logger.info("deleteMachine failed,some monitorTask is referring machine id={}  ",id);
            return Response.fail(ResponseCode.MACHINE_IS_REFERRED_BY_TASK);
        }
        boolean ok = monitorMachineService.deleteMachine(monitorMachine.getId());
        return Response.ok(ok);
    }


    /**
     * 新增机器
     *
     * @param monitorMachine
     * @return
     */
    @RequestMapping("/addMachine")
    public Response addMachine(@RequestBody MonitorMachine monitorMachine) {
        logger.info("addMachine start machineName={},machineIp={},operateSystemType={},projectId={},desc={}",
                monitorMachine.getMachineName(),
                monitorMachine.getMachineIp(),
                monitorMachine.getOperateSystemType(),
                monitorMachine.getProjectId(),
                monitorMachine.getMachineDesc());
//        Response response = paramVerification(monitorMachine);
//        if (response != null) {
//            return response;
//        }
        if (monitorMachineService.isExists(monitorMachine.getMachineName())) {
            logger.info("addMachine machineName={} has exists", monitorMachine.getMachineName());
            return Response.fail(ResponseCode.MACHINE_NAME_HAS_EXISTS);
        }

        boolean ok = monitorMachineService.addMachine(monitorMachine.getMachineName(),
                monitorMachine.getMachineIp(),
                monitorMachine.getOperateSystemType(),
                monitorMachine.getProjectId(),
                monitorMachine.getMachineDesc());
        logger.info("addMachine end machineName={},result={}", monitorMachine.getMachineName(), ok);
        return Response.ok(ok);
    }

    /**
     * 参数验证
     *
     * @param monitorMachine
     * @return
     */
//    private Response paramVerification(EditMonitorMachineVo monitorMachine) {
//        if (StringUtils.isBlank(monitorMachine.getMachineName())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "数据源名称不能为空");
//        }
//        if (StringUtils.isBlank(monitorMachine.getCutTemplate())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "切割模板不能为空");
//        }
//        if (StringUtils.isBlank(monitorMachine.getDataSourceLog())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "数据源日志不能为空");
//        }
//        if (StringUtils.isBlank(monitorMachine.getDataSourceServerIp())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "服务器IP不能为空");
//        }
//        String dataSourceServerIp = monitorMachine.getDataSourceServerIp();
//        String[] dataSourceServerIpArray = dataSourceServerIp.split(MonitorMachineConstant.comma);
//        Set<String> ips = new HashSet<>();
//        for (String ip : dataSourceServerIpArray) {
//            if (!VerifyUtil.isIP(ip)) {
//                logger.info("存在无效IP地址,ip={}", ip);
//                return Response.fail(ResponseCode.INVALID_IP, new Object[]{ip});
//            }
//            if (ips.contains(ip)) {
//                logger.info("addMachine 重复IP={}", ip);
//                return Response.fail(ResponseCode.IP_REPEAT, new Object[]{ip});
//            }
//            ips.add(ip);
//        }
//
//        if (MonitorMachineConstant.MonitorTomcatServer.YES == monitorMachine.getIsMonitorTomcatServer()
//                && StringUtils.isNotBlank(monitorMachine.getTomcatServerHost())) {
//            Map<String, List<Integer>> map = new HashMap<>();
//            String tomcatServerHostStr = monitorMachine.getTomcatServerHost();
//            String[] tomcatServerHostArray = tomcatServerHostStr.split(MonitorMachineConstant.comma);
//            for (String tomcatServerHost : tomcatServerHostArray) {
//                String[] ipPort = tomcatServerHost.split(MonitorMachineConstant.colon);
//                if (ipPort.length < 1) {
//                    logger.info("addMachine fail 存在无效的tomcat服务地址 {}", ipPort);
//                    return Response.fail(ResponseCode.INVALID_TOMCAT_ADDRESS, new Object[]{tomcatServerHost});
//                }
//                String ip = ipPort[0];
//                if (!ips.contains(ip)) {
//                    logger.info("监控服务地址和服务器地址IP不一致:{}", tomcatServerHost);
//                    return Response.fail(ResponseCode.IP_HAS_DIFFER, new Object[]{tomcatServerHost});
//                }
//                String portStr = ipPort[1];
//                int port = 0;
//                try {
//                    port = Integer.parseInt(portStr);
//                } catch (Exception e) {
//                    logger.info("无效端口 port={}", portStr);
//                }
//                if (port <= 0 || port > 65535) {
//                    logger.info("存在无效的端口{}", portStr);
//                    return Response.fail(ResponseCode.INVALID_PORT, new Object[]{portStr});
//
//                }
//                List<Integer> list = map.get(ip);
//                if (list == null) {
//                    list = new ArrayList<>();
//                    map.put(ip, list);
//                }
//                if (list.contains(port)) {
//                    logger.info("存在重复的端口{}", portStr);
//                    return Response.fail(ResponseCode.TOMCAT_ADDRESS_REPEAT, new Object[]{port});
//                }
//                list.add(port);
//            }
//        }
//        return null;
//    }

    /**
     * 编辑监控机器
     *
     * @param monitorMachine
     * @return
     */
    @RequestMapping("/editMachine")
    public Response editMachine(@RequestBody MonitorMachine monitorMachine) {
        logger.info("editMachine start machineId={} machineName={},projectId={},machineIp={},operateSystemType={},projectId={},desc={}",
                monitorMachine.getId(),
                monitorMachine.getMachineName(),
                monitorMachine.getProjectId(),
                monitorMachine.getMachineIp(),
                monitorMachine.getOperateSystemType(),
                monitorMachine.getProjectId(),
                monitorMachine.getMachineDesc());
//        Response response = paramVerification(monitorMachine);
//        if (response != null) {
//            return response;
//        }
        if (monitorMachine == null) {
            logger.info("editMachine fail id={} not exists", monitorMachine.getId());
            return Response.fail(ResponseCode.MACHINE_NOT_EXISTS);
        }
        if (monitorApplicationService.referMachine(monitorMachine.getId())) {
            logger.info("editMachine failed,some application is referring machine id={}",monitorMachine.getId());
            return Response.fail(ResponseCode.MACHINE_IS_REFERRED_BY_APPLICATION);
        }
        if(monitorTaskService.referMachine(monitorMachine.getId())){
            logger.info("deleteMachine failed,some monitorTask is referring machine id={}  ",monitorMachine.getId());
            return Response.fail(ResponseCode.MACHINE_IS_REFERRED_BY_TASK);
        }

        boolean ok = monitorMachineService.editMachine(monitorMachine.getId(), monitorMachine.getMachineName(), monitorMachine.getMachineIp(), monitorMachine.getOperateSystemType(),monitorMachine.getProjectId(), monitorMachine.getMachineDesc());
        logger.info("editMachine end machineId={},result={}", monitorMachine.getId(), ok);
        return Response.ok(ok);
    }


    //通过机器id查找
    @RequestMapping("/getMachineByMachineId")
    public Response getMachineByMachineId(@YXRequestParam(required = true, errmsg = "服务端根据机器Id查找发生错误(machineId不能为空)") Integer id) {
        logger.info("/getMachineByMachineId id:{}", id);
        MonitorMachine monitorMachine = monitorMachineService.selectOneByMachineId(id);
        return Response.ok(monitorMachine);
    }

    //搜索所有授权的机器
    @RequestMapping("/searchAllAuthorizeMachines")
    public Response searchAllAuthorizeMachines() {
        logger.info("/searchAllAuthorizeMachines");
        List<MonitorMachine> monitorMachinesList = monitorMachineService.selectAllAuthorizeMachines();
        return Response.ok(monitorMachinesList);
    }

    //根据项目Id搜索所有授权的机器
    @RequestMapping("/searchAllAuthorizeMachinesByProjectId")
    public Response searchAllAuthorizeMachinesByProjectId(@YXRequestParam(required = false, errmsg = "服务端根据项目Id搜索所有授权的机器发生错误(projectId不能为空)") Integer projectId) {
        logger.info("/searchAllAuthorizeMachines");
        List<MonitorMachine> monitorMachinesList = monitorMachineService.selectAllAuthorizeMachinesByProjectId(projectId);
        return Response.ok(monitorMachinesList);
    }





}