package com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex;

public interface SessionExMapper {

    Integer selectExpiryByKey(String key);
}