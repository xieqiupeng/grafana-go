package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.Star;

public interface StarMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Star record);

    int insertSelective(Star record);

    Star selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Star record);

    int updateByPrimaryKey(Star record);
}