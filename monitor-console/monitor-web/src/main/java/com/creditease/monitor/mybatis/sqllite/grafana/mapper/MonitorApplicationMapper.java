package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorApplication;

public interface MonitorApplicationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MonitorApplication record);

    int insertSelective(MonitorApplication record);

    MonitorApplication selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MonitorApplication record);

    int updateByPrimaryKey(MonitorApplication record);
}