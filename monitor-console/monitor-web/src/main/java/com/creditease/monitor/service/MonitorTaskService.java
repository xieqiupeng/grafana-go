package com.creditease.monitor.service;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.StarMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.UserMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorTaskExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import com.creditease.monitor.mybatis.sqllite.grafana.po.Star;
import com.creditease.monitor.mybatis.sqllite.grafana.po.User;
import com.creditease.response.Response;
import com.creditease.spring.annotation.YXRequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2017/12/12,at 14:54
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@Service
public class MonitorTaskService {
    private static Logger logger = LoggerFactory.getLogger(MonitorTaskService.class);

    @Autowired
    private MonitorTaskExMapper monitorTaskExMapper;


    public List selectByTaskName(String taskName){
        taskName="%"+taskName+"%";
        List<MonitorTask> monitorTasksList = monitorTaskExMapper.selectByTaskName(taskName);
        return monitorTasksList;
    }

}
