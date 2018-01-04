package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorMachine;

public interface MonitorMachineMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MonitorMachine record);

    int insertSelective(MonitorMachine record);

    MonitorMachine selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MonitorMachine record);

    int updateByPrimaryKey(MonitorMachine record);
}