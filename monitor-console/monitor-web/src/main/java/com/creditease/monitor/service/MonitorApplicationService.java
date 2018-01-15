package com.creditease.monitor.service;

import com.creditease.monitor.constant.MonitorApplicationConstant;
import com.creditease.monitor.constant.MonitorConstant;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorApplicationExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorMachineExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorProjectExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorTaskExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorApplication;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorMachine;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorProject;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
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

    @Autowired
    private MonitorTaskExMapper monitorTaskExMapper;

    @Autowired
    private MonitorEtcdService monitorApplicationEtcdService;

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

        }
    }


    /**
     * 删除Application
     * @param monitorApplication
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean deleteApplication(MonitorApplication monitorApplication) {
        //删除应用前,因为存在application共有的情况。在此处采用的策略是,
        //1、删除数据库记录
        //2、删除etc application home文件
        //3、根据当前项目的所有application,重新生成application home文件
        //4、删除当前项目下的所有task的etc文件，并且同时生成task的etc文件

        //1 删除数据库记录
        int count = monitorApplicationExMapper.deleteByPrimaryKey(monitorApplication.getId());
        if (count>0){
            updateAllReferenceEtcdKey(monitorApplication);
            return true;
        }
        return false;
    }

    //更新所有相关的EtcdKey
    private void updateAllReferenceEtcdKey(MonitorApplication monitorApplication){

        //2 删除etc application home文件
        monitorApplicationEtcdService.deleteApplicationHome(monitorApplication.getProjectId());
        //3 根据当前项目的所有application,重新生成application home文件
        List<MonitorApplication> monitorApplicationList = monitorApplicationExMapper.selectByProjectId(monitorApplication.getProjectId());
        if (monitorApplicationList!=null){
            monitorApplicationList.forEach((ma)->{
                monitorApplicationEtcdService.upSertApplicationHome(ma);
            });
        }
        //4、删除当前项目下的所有task的etc文件，并且同时生成task的etc文件
        List<MonitorTask> monitorTaskList = monitorTaskExMapper.selectByProjectId(monitorApplication.getProjectId());
        if (monitorTaskList!=null){
            for (int i=0;i<monitorTaskList.size();i++){
                MonitorTask monitorTask = monitorTaskList.get(i);
                if (monitorTask.getStatus()== MonitorConstant.MonitorTaskStatus.START){
                    monitorApplicationEtcdService.deleteTask(monitorTask.getProjectId(),monitorTask.getTaskName());
                    monitorApplicationEtcdService.upSertMonitorTask(monitorTask);
                }
            }
        }

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
    @Transactional(rollbackFor = {})
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

        monitorApplicationEtcdService.upSertApplicationHome(monitorApplication);

        return true;
    }

    /**
     * 修改application
     * @param monitorApplication
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean editApplication(MonitorApplication monitorApplication) {
        Date now = new Date();
        monitorApplication.setUpdateTime(now);
        int count = monitorApplicationExMapper.updateByPrimaryKeySelective(monitorApplication);
        if (count > 0) {
            updateAllReferenceEtcdKey(monitorApplication);
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

}
