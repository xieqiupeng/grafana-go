package com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorApplicationMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorApplication;

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2018/01/04,at 13:56
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
public interface MonitorApplicationExMapper extends MonitorApplicationMapper {

    List<MonitorApplication> selectByApplicationName(MonitorApplication monitorApplication);

    MonitorApplication selectOneByApplicationName(String applicationName);

    List<MonitorApplication> selectOneByMachineId(Integer machineId);

}