package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.Session;

public interface SessionMapper {
    int deleteByPrimaryKey(String key);

    int insert(Session record);

    int insertSelective(Session record);

    Session selectByPrimaryKey(String key);

    int updateByPrimaryKeySelective(Session record);

    int updateByPrimaryKeyWithBLOBs(Session record);

    int updateByPrimaryKey(Session record);
}