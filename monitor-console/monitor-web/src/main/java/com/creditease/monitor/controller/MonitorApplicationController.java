package com.creditease.monitor.controller;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorApplication;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.service.MonitorApplicationService;
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
 * @Date: created on 2018/01/04,at 15:05
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@RestController
@RequestMapping("monitorApplication")
public class MonitorApplicationController {
    private static Logger logger = LoggerFactory.getLogger(MonitorApplicationController.class);

    @Autowired
    private MonitorApplicationService monitorApplicationService;

    //通过ApplicationName模糊搜索
    @RequestMapping("/searchApplicationByApplicationName")
    public Response searchApplicationByApplicationName(@YXRequestParam(required = false, errmsg = "服务端根据应用名称模糊搜索发生错误(applicationName不能为空)") String applicationName,
                                         @YXRequestParam(required = false, errmsg = "服务端根据应用名称模糊搜索发生错误(pageNum不能为空)") Integer pageNum,
                                         @YXRequestParam(required = false, errmsg = "服务端根据应用名称模糊搜索发生错误(pageSize不能为空)") Integer pageSize) {
        logger.info("/searchApplicationByApplicationName param:applicationName:{},pageNum:{},pageSize:{}", applicationName, pageNum, pageSize);
        List<MonitorApplication> monitorApplicationsList = monitorApplicationService.selectByApplicationName(applicationName, pageNum, pageSize);

        //分页信息
        return Response.ok(new PageInfo(monitorApplicationsList));
    }


    //删除
    @RequestMapping("/deleteApplication")
    public Response deleteApplication(@YXRequestParam(required = true, errmsg = "服务端删除应用发生错误,id不能为空") Integer id) {
        logger.info("/deleteApplication id:{}", id);
        MonitorApplication monitorApplication = monitorApplicationService.selectOneByApplicationId(id);
        if (monitorApplication == null) {
            logger.info("deleteApplication fail id={} not exists", id);
            return Response.fail(ResponseCode.APPLICATION_NOT_EXISTS);
        }
        boolean ok = monitorApplicationService.deleteApplication(monitorApplication.getId());
        return Response.ok(ok);
    }


    /**
     * 新增应用
     *
     * @param monitorApplication
     * @return
     */
    @RequestMapping("/addApplication")
    public Response addApplication(@RequestBody MonitorApplication monitorApplication) {
        logger.info("addApplication start applicationName={},applicationType={},applicationDetailParam={},desc={}",
                monitorApplication.getApplicationName(),
                monitorApplication.getApplicationType(),
                monitorApplication.getApplicationDetailParam(),
                monitorApplication.getDesc());
//        Response response = paramVerification(monitorApplication);
//        if (response != null) {
//            return response;
//        }
        if (monitorApplicationService.isExists(monitorApplication.getApplicationName())) {
            logger.info("addApplication applicationName={} has exists", monitorApplication.getApplicationName());
            return Response.fail(ResponseCode.MACHINE_NAME_HAS_EXISTS);
        }

        boolean ok = monitorApplicationService.addApplication(monitorApplication.getApplicationName(),
                monitorApplication.getApplicationType(),
                monitorApplication.getApplicationDetailParam(),
                monitorApplication.getDesc());
        logger.info("addApplication end applicationName={},result={}", monitorApplication.getApplicationName(), ok);
        return Response.ok(ok);
    }

    /**
     * 参数验证
     *
     * @param monitorApplication
     * @return
     */
//    private Response paramVerification(EditMonitorApplicationVo monitorApplication) {
//        if (StringUtils.isBlank(monitorApplication.getApplicationName())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "数据源名称不能为空");
//        }
//        if (StringUtils.isBlank(monitorApplication.getCutTemplate())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "切割模板不能为空");
//        }
//        if (StringUtils.isBlank(monitorApplication.getDataSourceLog())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "数据源日志不能为空");
//        }
//        if (StringUtils.isBlank(monitorApplication.getDataSourceServerIp())) {
//            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "服务器IP不能为空");
//        }
//        String dataSourceServerIp = monitorApplication.getDataSourceServerIp();
//        String[] dataSourceServerIpArray = dataSourceServerIp.split(MonitorApplicationConstant.comma);
//        Set<String> ips = new HashSet<>();
//        for (String ip : dataSourceServerIpArray) {
//            if (!VerifyUtil.isIP(ip)) {
//                logger.info("存在无效IP地址,ip={}", ip);
//                return Response.fail(ResponseCode.INVALID_IP, new Object[]{ip});
//            }
//            if (ips.contains(ip)) {
//                logger.info("addApplication 重复IP={}", ip);
//                return Response.fail(ResponseCode.IP_REPEAT, new Object[]{ip});
//            }
//            ips.add(ip);
//        }
//
//        if (MonitorApplicationConstant.MonitorTomcatServer.YES == monitorApplication.getIsMonitorTomcatServer()
//                && StringUtils.isNotBlank(monitorApplication.getTomcatServerHost())) {
//            Map<String, List<Integer>> map = new HashMap<>();
//            String tomcatServerHostStr = monitorApplication.getTomcatServerHost();
//            String[] tomcatServerHostArray = tomcatServerHostStr.split(MonitorApplicationConstant.comma);
//            for (String tomcatServerHost : tomcatServerHostArray) {
//                String[] ipPort = tomcatServerHost.split(MonitorApplicationConstant.colon);
//                if (ipPort.length < 1) {
//                    logger.info("addApplication fail 存在无效的tomcat服务地址 {}", ipPort);
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
     * 编辑监控应用
     *
     * @param monitorApplication
     * @return
     */
    @RequestMapping("/editApplication")
    public Response editApplication(@RequestBody MonitorApplication monitorApplication) {
        logger.info("editApplication start applicationName={},applicationType={},applicationDetailParam={},desc={}",
                monitorApplication.getApplicationName(),
                monitorApplication.getApplicationType(),
                monitorApplication.getApplicationDetailParam(),
                monitorApplication.getDesc());
//        Response response = paramVerification(monitorApplication);
//        if (response != null) {
//            return response;
//        }
        if (monitorApplication == null) {
            logger.info("editApplication fail id={} not exists", monitorApplication.getId());
            return Response.fail(ResponseCode.APPLICATION_NOT_EXISTS);
        }

        boolean ok = monitorApplicationService.editApplication(monitorApplication.getId(),  monitorApplication.getApplicationType(), monitorApplication.getApplicationDetailParam(), monitorApplication.getDesc());
        logger.info("editApplication end applicationId={},result={}", monitorApplication.getId(), ok);
        return Response.ok(ok);
    }
}