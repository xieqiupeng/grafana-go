package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;

public interface MonitorTaskMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MonitorTask record);

    int insertSelective(MonitorTask record);

    MonitorTask selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MonitorTask record);

    int updateByPrimaryKey(MonitorTask record);
}