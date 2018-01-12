package com.creditease.monitor.controller;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorProject;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.service.MonitorMachineService;
import com.creditease.monitor.service.MonitorProjectService;
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

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2018/01/04,at 10:54
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@RestController
@RequestMapping("monitorProject")
public class MonitorProjectController {
    private static Logger logger = LoggerFactory.getLogger(MonitorProjectController.class);

    @Autowired
    private MonitorProjectService monitorProjectService;

    @Autowired
    private MonitorMachineService monitorMachineService;

    //通过ProjectName模糊搜索
    @RequestMapping("/searchProjectByProjectName")
    public Response searchProjectByProjectName(@YXRequestParam(required = false, errmsg = "服务端根据项目名称模糊搜索发生错误(projectName不能为空)") String projectName,
                                         @YXRequestParam(required = false, errmsg = "服务端根据项目名称模糊搜索发生错误(pageNum不能为空)") Integer pageNum,
                                         @YXRequestParam(required = false, errmsg = "服务端根据项目名称模糊搜索发生错误(pageSize不能为空)") Integer pageSize) {
        logger.info("/searchProjectByProjectName param:projectName:{},pageNum:{},pageSize:{}", projectName, pageNum, pageSize);
        List<MonitorProject> monitorProjectsList = monitorProjectService.selectByProjectName(projectName, pageNum, pageSize);

        //分页信息
        return Response.ok(new PageInfo(monitorProjectsList));
    }

    //删除
    @RequestMapping("/deleteProject")
    public Response deleteProject(@YXRequestParam(required = true, errmsg = "服务端删除任务发生错误") Integer id  ) {
        logger.info("/deleteProject id:{}", id);
        MonitorProject monitorProject = monitorProjectService.selectOneByProjectId(id);
        if (monitorProject == null) {
            logger.info("deleteProject fail id={} not exists", id);
            return Response.fail(ResponseCode.PROJECT_NOT_EXISTS);
        }

        if(true==monitorMachineService.referProject(monitorProject.getId())){
            logger.info("deleteProject fail,some machine is referring project id={} ", monitorProject.getId());
            return Response.fail(ResponseCode.PROJECT_IS_REFERRED_BY_MACHINE_LIST);
        }

        boolean ok = monitorProjectService.deleteProject(monitorProject.getId());
        return Response.ok(ok);
    }



    /**
     * 新增项目
     * @param monitorProject
     * @return
     */
    @RequestMapping("/addProject")
    public Response addProject(@RequestBody  MonitorProject monitorProject) {
        logger.info("addProject start projectName={},cutTemplate={},dataSourceLog,dataSourceServerIp={},isMonitorTomcatServer={},tomcatServerHost",
                monitorProject.getProjectName(),
                monitorProject.getProjectDesc()
                );

        Response response = paramVerification(monitorProject);
        if (response != null) {
            return response;
        }

        if (monitorProjectService.isExists(monitorProject.getProjectName())) {
            logger.info("addProject projectName={} has exists", monitorProject.getProjectName());
            return Response.fail(ResponseCode.PROJECT_NAME_HAS_EXISTS);
        }
        boolean ok = monitorProjectService.addProject(monitorProject.getProjectName(),monitorProject.getProjectDesc());
        logger.info("addProject end projectName={},result={}", monitorProject.getProjectName(), ok);
        return Response.ok(ok);
    }

    /**
     * 参数验证
     *
     * @param monitorProject
     * @return
     */
    private Response paramVerification(MonitorProject monitorProject) {
        if (StringUtils.isBlank(monitorProject.getProjectName())) {
            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "项目名称不能为空");
        }
        return null;
    }

    /**
     * 编辑监控项目
     * @param monitorProject
     * @return
     */
    @RequestMapping("/editProject")
    public Response editProject(@RequestBody MonitorProject monitorProject) {
        logger.info("editProject start projectId={},projectName={},desc={}",
                monitorProject.getId(),
                monitorProject.getProjectName(),
                monitorProject.getProjectDesc());
        Response response = paramVerification(monitorProject);
        if (response != null) {
            return response;
        }
        if (null==monitorProject.getId()) {
            return Response.fail(BaseResultCode.COMMON_HTTP_PARAM_RESOVE_OR_VALIDATE_ERROR, "项目Id不能为空");
        }
        if(true==monitorMachineService.referProject(monitorProject.getId())){
            logger.info("editProject fail,some machine is referring project id={} ", monitorProject.getId());
            return Response.fail(ResponseCode.PROJECT_IS_REFERRED_BY_MACHINE_LIST);
        }
        boolean ok = monitorProjectService.editProject(monitorProject.getId(),monitorProject.getProjectDesc());
        logger.info("editProject end id={},result={}", monitorProject.getId(), ok);
        return Response.ok(ok);
    }



    //通过id查找
    @RequestMapping("/getProjectByProjectId")
    public Response getProjectByProjectId(@YXRequestParam(required = true, errmsg = "服务端根据项目名称查找发生错误(taskName不能为空)") Integer id) {
        logger.info("/getProjectByProjectId id:{}", id);
        MonitorProject monitorProject = monitorProjectService.selectOneByProjectId(id);
        return Response.ok(monitorProject);
    }

    //搜索所有授权的项目
    @RequestMapping("/searchAllAuthorizeProjects")
    public Response searchAllAuthorizeProjects() {
        logger.info("/searchAllAuthorizeProjects");
        List<MonitorProject> monitorProjectsList = monitorProjectService.selectAllAuthorizeProjects();
        return Response.ok(monitorProjectsList);
    }

}