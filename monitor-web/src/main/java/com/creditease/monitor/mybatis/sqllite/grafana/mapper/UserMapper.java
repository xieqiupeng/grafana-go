package com.creditease.monitor.mybatis.sqllite.grafana.mapper;

import com.creditease.monitor.mybatis.sqllite.grafana.po.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
}