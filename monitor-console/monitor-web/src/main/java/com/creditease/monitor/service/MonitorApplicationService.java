package com.creditease.monitor.service;

import com.creditease.monitor.constant.MonitorApplicationConstant;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorApplicationExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorMachineExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorProjectExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorApplication;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorMachine;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorProject;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2018/01/04,at 13:54
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@Service
public class MonitorApplicationService {
    private static Logger logger = LoggerFactory.getLogger(MonitorApplicationService.class);
    @Autowired
    private MonitorApplicationExMapper monitorApplicationExMapper;

    @Autowired
    private MonitorMachineExMapper monitorMachineExMapper;

    @Autowired
    private MonitorProjectExMapper monitorProjectExMapper;

    /**
     * 根据应用名称模糊查找
     *
     * @param applicationName
     * @return
     */
    public List selectByApplicationName(String applicationName,Integer projectId,Integer machineId, Integer pageNum, Integer pageSize) {
        //设置参数
        if (applicationName == null) {
            applicationName = "";
        }
        applicationName = "%" + applicationName + "%";
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //执行查询
        MonitorApplication monitorApplication=new MonitorApplication();
        monitorApplication.setApplicationName(applicationName);
        monitorApplication.setProjectId(projectId);
        monitorApplication.setMachineId(machineId);
        List<MonitorApplication> monitorApplicationsList = monitorApplicationExMapper.selectByApplicationName(monitorApplication);
        dealWithMonitorApplicationsList(monitorApplicationsList);
        return monitorApplicationsList;
    }

    private void dealWithMonitorApplicationsList(List<MonitorApplication> monitorApplicationsList){
        if (monitorApplicationsList==null){
            return ;
        }
        for (int i=0;i<monitorApplicationsList.size();i++){
            MonitorApplication monitorApplication = monitorApplicationsList.get(i);
            MonitorMachine monitorMachine = monitorMachineExMapper.selectByPrimaryKey(monitorApplication.getMachineId());
            MonitorProject monitorProject = monitorProjectExMapper.selectByPrimaryKey(monitorApplication.getProjectId());
            monitorApplication.setMachineName(monitorMachine==null?"未知机器":monitorMachine.getMachineName());
            monitorApplication.setProjectName(monitorProject==null?"未知项目":monitorProject.getProjectName());

            if(monitorApplication.getStatus()==MonitorApplicationConstant.MonitorApplicationStatus.START){
                //启动状态
                monitorApplication.setStatusText(MonitorApplicationConstant.MonitorApplicationStatus.STARTTEXT);
            }else if(monitorApplication.getStatus()==MonitorApplicationConstant.MonitorApplicationStatus.PAUSE){
                //暂停状态
                monitorApplication.setStatusText(MonitorApplicationConstant.MonitorApplicationStatus.PAUSETEXT);
            }else{
                //未知状态
                monitorApplication.setStatusText("未知状态");
            }

        }
    }


    /**
     * 删除Application
     *
     * @param applicationId
     * @return
     */
    public boolean deleteApplication(int applicationId) {
        //删除当前监控项目

        monitorApplicationExMapper.deleteByPrimaryKey(applicationId);
        return true;
    }

    public MonitorApplication selectOneByApplicationId(int applicationId) {
        return monitorApplicationExMapper.selectByPrimaryKey(applicationId);
    }

    /**
     * 添加应用
     *
     * @param applicationName
     * @param desc
     * @return
     */

    public boolean addApplication(String applicationName,
                           Integer projectId,
                           Integer machineId,
                           Byte applicationType,
                           String applicationDetailParam,
                           String desc) {
        MonitorApplication monitorApplication = new MonitorApplication();
        monitorApplication.setApplicationName(applicationName);
        monitorApplication.setProjectId(projectId);
        monitorApplication.setMachineId(machineId);
        monitorApplication.setApplicationType(applicationType);
        monitorApplication.setApplicationDetailParam(applicationDetailParam);
        monitorApplication.setApplicationDesc(desc);
        Date now = new Date();
        monitorApplication.setCreateTime(now);
        monitorApplication.setUpdateTime(now);
        monitorApplicationExMapper.insertSelective(monitorApplication);
        return true;
    }

    /**
     * 修改application
     *
     * @param id
     * @param applicationType
     * @param applicationDetailParam
     * @param desc
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean editApplication(Integer id,
                                   Byte applicationType,
                                   String applicationDetailParam,
                                   String desc
                            ) {
        MonitorApplication monitorApplication = new MonitorApplication();
        Date now = new Date();
        monitorApplication.setId(id);
        monitorApplication.setApplicationType(applicationType);
        monitorApplication.setApplicationDesc(desc);
        monitorApplication.setApplicationDetailParam(applicationDetailParam);
        monitorApplication.setUpdateTime(now);
        int count = monitorApplicationExMapper.updateByPrimaryKeySelective(monitorApplication);
        if (count > 0) {
            return true;
        }
        return false;
    }


    public boolean referMachine(Integer machineId) {

        List<MonitorApplication> monitorApplicationList = monitorApplicationExMapper.selectOneByMachineId(machineId);
        if(monitorApplicationList!=null&&monitorApplicationList.size()>0){
            return true;
        }
        return false;
    }

    public MonitorApplication selectOneByApplicationName(String projectName) {
        return monitorApplicationExMapper.selectOneByApplicationName(projectName);
    }

    public boolean isExists(String projectName) {
        return selectOneByApplicationName(projectName) == null ? false : true;
    }

    public boolean startOrPauseApplication(MonitorApplication monitorApplication) {
        MonitorApplication newStatus = new MonitorApplication();
        newStatus.setId(monitorApplication.getId());
        newStatus.setStatus(monitorApplication.getStatus());
        newStatus.setUpdateTime(new Date());
        monitorApplicationExMapper.updateByPrimaryKeySelective(newStatus);
        return true;
    }
}
