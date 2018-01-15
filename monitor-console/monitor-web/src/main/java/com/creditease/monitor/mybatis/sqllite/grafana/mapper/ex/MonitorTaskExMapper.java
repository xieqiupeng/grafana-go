package com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorTaskMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2017/12/12,at 16:16
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
public interface MonitorTaskExMapper extends MonitorTaskMapper {

    List<MonitorTask> selectByTaskName(String taskName);

    List<MonitorTask> selectByProjectId(Integer projectId);

    MonitorTask selectOneByTaskName(String taskName);

    List<MonitorTask> selectOneByMachineId(Integer machineId);

}