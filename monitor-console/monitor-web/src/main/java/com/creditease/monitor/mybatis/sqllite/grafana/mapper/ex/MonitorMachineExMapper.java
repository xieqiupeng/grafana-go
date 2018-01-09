package com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorMachineMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorMachine;

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2018/01/04,at 11:50
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
public interface MonitorMachineExMapper extends MonitorMachineMapper {

    List<MonitorMachine> selectByMachineNameAndProjectId(String machineName,Integer projectId);

    List<MonitorMachine> selectByMachineName(String machineName);

    MonitorMachine selectOneByMachineName(String machineName);

    List<MonitorMachine> selectOneByProjectId(Integer projectId);

    List<MonitorMachine> selectAllAuthorizeMachines();

    List<MonitorMachine> selectAllAuthorizeMachinesByProjectId(Integer projectId);

}