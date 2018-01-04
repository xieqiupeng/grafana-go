package com.creditease.monitor.service;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorTask2ExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask2;
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
public class MonitorTask2Service {
    private static Logger logger = LoggerFactory.getLogger(MonitorTask2Service.class);
    @Autowired
    private MonitorTask2ExMapper monitorTask2ExMapper;

    /**
     * 根据任务名称模糊查找
     *
     * @param taskName
     * @return
     */
    public List selectByTaskName(String taskName, Integer pageNum, Integer pageSize) {
        //设置参数
        if (taskName == null) {
            taskName = "";
        }
        taskName = "%" + taskName + "%";
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //执行查询
        List<MonitorTask2> monitorTasksList = monitorTask2ExMapper.selectByTaskName(taskName);

        return monitorTasksList;
    }


    /**
     * 删除Task
     *
     * @param taskId
     * @return
     */
    public boolean deleteTask(int taskId) {
        //删除当前监控项目

        monitorTask2ExMapper.deleteByPrimaryKey(taskId);
        return true;
    }

    public MonitorTask2 selectOneByTaskId(int taskId) {
        return monitorTask2ExMapper.selectByPrimaryKey(taskId);
    }

    /**
     * 添加任务
     *
     * @param taskName
     * @param desc
     * @return
     */
    public boolean addTask(String taskName,
                           String desc) {
        MonitorTask2 monitorTask = new MonitorTask2();
        monitorTask.setTaskName(taskName);
        Date now = new Date();
        monitorTask.setCreateTime(now);
        monitorTask.setUpdateTime(now);

        monitorTask2ExMapper.insertSelective(monitorTask);
        return true;
    }

    /**
     * 修改task
     *
     * @param id
     * @param taskName
     * @param taskDesc
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean editTask(Integer id,
                            String taskName,
                            String taskDesc
                            ) {
        MonitorTask2 monitorTask = new MonitorTask2();
        Date now = new Date();
        monitorTask.setId(id);
        monitorTask.setTaskName(taskName);
        monitorTask.setUpdateTime(now);
        int count = monitorTask2ExMapper.updateByPrimaryKeySelective(monitorTask);
        if (count > 0) {
            return true;
        }
        return false;
    }

}
