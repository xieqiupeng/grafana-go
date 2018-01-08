package com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.MonitorProjectMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorProject;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2018/01/04,at 11:16
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
public interface MonitorProjectExMapper extends MonitorProjectMapper {


    List<MonitorProject> selectAllAuthorizeProjects();
    List<MonitorProject> selectByProjectName(String projectName);
    MonitorProject selectOneByProjectName(String projectName);
}