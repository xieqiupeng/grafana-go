package com.creditease.monitor.service;

import com.creditease.monitor.enums.MonitorTaskStatus;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorTaskMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.StarMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.UserMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorTaskExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.mybatis.sqllite.grafana.po.Star;
import com.creditease.monitor.mybatis.sqllite.grafana.po.User;
import com.creditease.response.Response;
import com.creditease.spring.annotation.YXRequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
@Service
public class MonitorTaskService {
    private static Logger logger = LoggerFactory.getLogger(MonitorTaskService.class);

    @Autowired
    private MonitorTaskExMapper monitorTaskExMapper;

    @Autowired
    private MonitorTaskMapper monitorTaskMapper;

    /**
     * 根据任务名称模糊查找
     * @param taskName
     * @return
     */
    public List selectByTaskName(String taskName){
        if(taskName==null){
            taskName="";
        }
        taskName="%"+taskName+"%";
        List<MonitorTask> monitorTasksList = monitorTaskExMapper.selectByTaskName(taskName);
        dealWithMonitorTaskList(monitorTasksList);
        return monitorTasksList;
    }

    /**
     * 启动/暂停
     * @param taskId
     * @return
     */
    public void startOrPauseTask(Integer taskId){
        //查询当前监控任务
        MonitorTask monitorTask = monitorTaskMapper.selectByPrimaryKey(taskId);
        //启动/暂停状态发生切换
        if(monitorTask.getStatus().equals(MonitorTaskStatus.START.getValue())){
            monitorTask.setStatus(MonitorTaskStatus.PAUSE.getValue());
        }else if(monitorTask.getStatus().equals(MonitorTaskStatus.PAUSE.getValue())){
            monitorTask.setStatus(MonitorTaskStatus.START.getValue());
        }
        monitorTaskMapper.updateByPrimaryKeySelective(monitorTask);
    }

    /**
     * 删除
     * @param taskId
     * @return
     */
    public void deleteTask(Integer taskId) {
        //删除当前监控任务
        monitorTaskMapper.deleteByPrimaryKey(taskId);

    }


    /**
     * 预处理
     * @param monitorTasksList
     */
    private void dealWithMonitorTaskList(List<MonitorTask> monitorTasksList){
        if(monitorTasksList!=null){
            for(int i=0;i<monitorTasksList.size();i++){
                if(monitorTasksList.get(i).getStatus().equals(MonitorTaskStatus.START.getValue())){
                    monitorTasksList.get(i).setStatus(MonitorTaskStatus.START.getMsg());
                }else if (monitorTasksList.get(i).getStatus().equals(MonitorTaskStatus.PAUSE.getValue())){
                    monitorTasksList.get(i).setStatus(MonitorTaskStatus.PAUSE.getMsg());
                }
            }
        }
    }

}
