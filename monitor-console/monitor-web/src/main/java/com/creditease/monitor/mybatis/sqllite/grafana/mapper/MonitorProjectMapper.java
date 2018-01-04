package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorProject;

public interface MonitorProjectMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MonitorProject record);

    int insertSelective(MonitorProject record);

    MonitorProject selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MonitorProject record);

    int updateByPrimaryKey(MonitorProject record);
}