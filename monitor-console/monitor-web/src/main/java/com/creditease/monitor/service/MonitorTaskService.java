package com.creditease.monitor.service;

import com.creditease.monitor.dataclean.DataCleanRuleEntity;
import com.creditease.monitor.dataclean.DataCleanUtil;
import com.creditease.monitor.dataclean.IDataCleanRule;
import com.creditease.monitor.enums.MonitorTaskStatus;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorTaskMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorTaskExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.vo.CutExampleVo;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        //预处理
        dealWithMonitorTaskList(monitorTasksList);
        return monitorTasksList;
    }

    /**
     * 启动/暂停
     * @param taskId
     * @return
     */
    public boolean startOrPauseTask(Integer taskId,byte status){
        //查询当前监控任务
        MonitorTask monitorTask = monitorTaskMapper.selectByPrimaryKey(taskId);
        if(monitorTask != null ){
            boolean isStart = false;
            //启动/暂停状态发生切换
            if(MonitorTaskStatus.START.getValue() == status){
                monitorTask.setStatus(MonitorTaskStatus.PAUSE.getValue());
                isStart = true;
            }else if(MonitorTaskStatus.PAUSE.getValue() == status){
                monitorTask.setStatus(MonitorTaskStatus.START.getValue());
            }else{
                return false;
            }
            boolean ok;
            if(isStart){
                ok = monitorTaskEtcdService.upSert(monitorTask);
            }else{
                ok = monitorTaskEtcdService.delete(monitorTask.getTaskName());
            }
            if(ok){
                monitorTaskMapper.updateByPrimaryKeySelective(monitorTask);
                return true;
            }
        }
        return false;
    }

    /**
     * 删除
     * @param taskId
     * @return
     */
    public boolean deleteTask(Integer taskId) {
        MonitorTask monitorTask = monitorTaskMapper.selectByPrimaryKey(taskId);
        if(monitorTask != null){
            boolean ok = monitorTaskEtcdService.delete(monitorTask.getTaskName());
            if(ok){
                //删除当前监控任务
                monitorTaskMapper.deleteByPrimaryKey(taskId);
                return ok;
            }
        }
        return false;
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

    /**
     * 预处理
     * @param monitorTasksList
     */
    private void dealWithMonitorTaskList(List<MonitorTask> monitorTasksList){
//        if(monitorTasksList!=null){
//            for(int i=0;i<monitorTasksList.size();i++){
//                if(monitorTasksList.get(i).getStatus().equals(MonitorTaskStatus.START.getValue())){
//                    monitorTasksList.get(i).setStatus(MonitorTaskStatus.START.getMsg());
//                }else if (monitorTasksList.get(i).getStatus().equals(MonitorTaskStatus.PAUSE.getValue())){
//                    monitorTasksList.get(i).setStatus(MonitorTaskStatus.PAUSE.getMsg());
//                }
//            }
//        }
    }

}
