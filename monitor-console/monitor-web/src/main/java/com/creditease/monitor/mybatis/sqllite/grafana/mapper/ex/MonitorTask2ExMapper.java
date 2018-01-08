package com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorTask2Mapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorTaskMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask2;

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2017/12/12,at 16:16
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
public interface MonitorTask2ExMapper extends MonitorTask2Mapper {

    List<MonitorTask2> selectByTaskName(String taskName);

    MonitorTask2 selectOneByTaskName(String taskName);

    List<MonitorTask2> selectOneByMachineId(Integer machineId);

}