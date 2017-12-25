package com.creditease.monitor.service;

import com.alibaba.fastjson.JSON;
import com.creditease.monitor.dataclean.DataCleanRuleEntity;
import com.creditease.monitor.dataclean.DataCleanUtil;
import com.creditease.monitor.dataclean.IDataCleanRule;
import com.creditease.monitor.constant.MonitorTaskConstant;
import com.creditease.monitor.exception.MonitorTaskException;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorTaskExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.vo.CutExampleVo;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2017/12/12,at 14:54
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@Service
public class MonitorTaskService {
    private static Logger logger = LoggerFactory.getLogger(MonitorTaskService.class);
    @Autowired
    private MonitorTaskExMapper monitorTaskExMapper;
    @Autowired
    private MonitorTaskEtcdService monitorTaskEtcdService;
    /**
     * 根据任务名称模糊查找
     * @param taskName
     * @return
     */
    public List selectByTaskName(String taskName,Integer pageNum,Integer pageSize){
        //设置参数
        if(taskName==null){
            taskName="";
        }
        taskName="%"+taskName+"%";
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //执行查询
        List<MonitorTask> monitorTasksList = monitorTaskExMapper.selectByTaskName(taskName);

        return monitorTasksList;
    }

    /**
     * 启动/暂停
     * @param monitorTask
     * @return
     */
    @Transactional
    public boolean startOrPauseTask(MonitorTask monitorTask){
        if(monitorTask != null){
            //查询当前监控任务
            boolean isStart;
            //启动/暂停状态发生切换
            if(MonitorTaskConstant.MonitorTaskStatus.START == monitorTask.getStatus()){
                isStart = true;
            }else if(MonitorTaskConstant.MonitorTaskStatus.PAUSE == monitorTask.getStatus()){
                isStart = false;
            }else{
                return false;
            }

            MonitorTask newStatus = new MonitorTask();
            newStatus.setId(monitorTask.getId());
            newStatus.setStatus(monitorTask.getStatus());
            newStatus.setUpdateTime(new Date());
            int count = monitorTaskExMapper.updateByPrimaryKeySelective(newStatus);
            if(count > 0){
                if(isStart){
                    logger.info("同步ETCD数据源成功 monitorTask={}",JSON.toJSONString(monitorTask));
                    boolean ok = monitorTaskEtcdService.upSert(monitorTask);
                    if(!ok){
                        throw new MonitorTaskException(ResponseCode.START_TASK_ERROR,StringUtils.EMPTY);
                    }
                }else{
                    logger.info("删除ETCD数据源成功 monitorTaskName={}",monitorTask.getTaskName());
                    boolean ok = monitorTaskEtcdService.delete(monitorTask.getTaskName());
                    if(!ok){
                        throw new MonitorTaskException(ResponseCode.PAUSE_TASK_ERROR,StringUtils.EMPTY);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 删除Task
     * @param taskId
     * @return
     */
    public boolean deleteTask(int taskId) {
            //删除当前监控任务
        monitorTaskExMapper.deleteByPrimaryKey(taskId);
        return true;
    }

    /**
     * 数据清洗
     * @param monitorDates
     * @param dataCleanRuleEntity
     * @return
     */
    public List<CutExampleVo> dataClean(List<String> monitorDates,DataCleanRuleEntity dataCleanRuleEntity){
        List<CutExampleVo> vos = new ArrayList<>();
        if(monitorDates != null && !monitorDates.isEmpty() && dataCleanRuleEntity != null){
            IDataCleanRule dataCleanRule = DataCleanUtil.getDataCleanRule(dataCleanRuleEntity);
            monitorDates.forEach(monitorDate->{
                if(StringUtils.isNotBlank(monitorDate)){
                    vos.addAll(dataCleanRule.clean(monitorDate));
                }
            });
        }
        return vos;
    }
    public boolean isExists(String taskName){
         return  selectOneByTaskName(taskName) == null ? false : true;
    }

    public boolean isExists(int taskId){
        return  selectOneByTaskId(taskId) == null ? false : true;
    }

    public MonitorTask selectOneByTaskName(String taskName){
        return monitorTaskExMapper.selectOneByTaskName(taskName);
    }

    public MonitorTask selectOneByTaskId(int taskId){
        return monitorTaskExMapper.selectByPrimaryKey(taskId);
    }

    /**
     * 添加任务
     * @param taskName
     * @param cutTemplate
     * @param dataSourceLog
     * @param dataSourceServerIp
     * @param isMonitorTomcatServer
     * @param tomcatServerHost
     * @return
     */
    public boolean addTask(String taskName,
                           String cutTemplate,
                           String dataSourceLog,
                           String dataSourceServerIp,
                           byte isMonitorTomcatServer,
                           String tomcatServerHost){
        MonitorTask monitorTask = new MonitorTask();
        Date now = new Date();
        monitorTask.setCreateTime(now);
        monitorTask.setUpdateTime(now);
        monitorTask.setTaskName(taskName);
        monitorTask.setCutTemplate(cutTemplate);
        monitorTask.setDataSourceLog(dataSourceLog);
        monitorTask.setDataSourceServerIp(dataSourceServerIp);
        monitorTask.setStatus(MonitorTaskConstant.MonitorTaskStatus.PAUSE);
        if(MonitorTaskConstant.MonitorTomcatServer.YES == isMonitorTomcatServer){
            monitorTask.setIsMonitorTomcatServer(MonitorTaskConstant.MonitorTomcatServer.YES);
            monitorTask.setTomcatServerHost(tomcatServerHost);
        }else{
            monitorTask.setIsMonitorTomcatServer(MonitorTaskConstant.MonitorTomcatServer.NO);
        }
        monitorTaskExMapper.insertSelective(monitorTask);
        return true;
    }

    /**
     * 修改task
     * @param cutTemplate
     * @param dataSourceLog
     * @param dataSourceServerIp
     * @param isMonitorTomcatServer
     * @param tomcatServerHost
     * @return
     */
    @Transactional
    public boolean editTask(int taskId,
                           String cutTemplate,
                           String dataSourceLog,
                           String dataSourceServerIp,
                           byte isMonitorTomcatServer,
                           String tomcatServerHost){
        MonitorTask monitorTask = new MonitorTask();
        Date now = new Date();
        monitorTask.setId(taskId);
        monitorTask.setUpdateTime(now);
        monitorTask.setCutTemplate(cutTemplate);
        monitorTask.setDataSourceLog(dataSourceLog);
        monitorTask.setDataSourceServerIp(dataSourceServerIp);
        if(MonitorTaskConstant.MonitorTomcatServer.YES == isMonitorTomcatServer){
            monitorTask.setIsMonitorTomcatServer(MonitorTaskConstant.MonitorTomcatServer.YES);
            monitorTask.setTomcatServerHost(tomcatServerHost);
        }else{
            monitorTask.setIsMonitorTomcatServer(MonitorTaskConstant.MonitorTomcatServer.NO);
        }
        int count =  monitorTaskExMapper.updateByPrimaryKeySelective(monitorTask);
        if(count > 0){
            return true;
        }
        return false;
    }

}
