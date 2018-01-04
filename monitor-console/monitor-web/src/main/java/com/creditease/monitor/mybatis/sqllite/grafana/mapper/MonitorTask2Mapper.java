package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask2;

public interface MonitorTask2Mapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MonitorTask2 record);

    int insertSelective(MonitorTask2 record);

    MonitorTask2 selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MonitorTask2 record);

    int updateByPrimaryKey(MonitorTask2 record);
}