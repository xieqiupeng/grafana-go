package com.creditease.monitor.service;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorMachineExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorMachine;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author: created by zhixinsong2
 * @Date: created on 2018/01/04,at 11:05
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@Service
public class MonitorMachineService {
    private static Logger logger = LoggerFactory.getLogger(MonitorMachineService.class);
    @Autowired
    private MonitorMachineExMapper monitorMachineExMapper;


    /**
     * 根据任务名称模糊查找
     *
     * @param machineName
     * @return
     */
    public List selectByMachineName(String machineName, Integer pageNum, Integer pageSize) {
        //设置参数
        if (machineName == null) {
            machineName = "";
        }
        machineName = "%" + machineName + "%";
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //执行查询
        List<MonitorMachine> monitorMachinesList = monitorMachineExMapper.selectByMachineName(machineName);

        return monitorMachinesList;
    }

    /**
     * 删除Machine
     *
     * @param machineId
     * @return
     */
    public boolean deleteMachine(int machineId) {
        //删除当前监控任务
        monitorMachineExMapper.deleteByPrimaryKey(machineId);
        return true;
    }


    public boolean referProject(int projectId) {
        return true;
    }


    public MonitorMachine selectOneByMachineName(String machineName) {
        return monitorMachineExMapper.selectOneByMachineName(machineName);
    }

    public MonitorMachine selectOneByMachineId(int machineId) {
        return monitorMachineExMapper.selectByPrimaryKey(machineId);
    }

    /**
     * 添加机器
     * @param machineName
     * @param machineIp
     * @param operateSystemType
     * @param desc
     * @return
     */
    public boolean addMachine(String machineName,
                           String machineIp,
                           Byte operateSystemType,
                           String desc) {
        MonitorMachine monitorMachine = new MonitorMachine();
        Date now = new Date();
        monitorMachine.setMachineName(machineName);
        monitorMachine.setMachineIp(machineIp);
        monitorMachine.setOperateSystemType(operateSystemType);
        monitorMachine.setDesc(desc);
        monitorMachine.setCreateTime(now);
        monitorMachine.setUpdateTime(now);
        monitorMachineExMapper.insertSelective(monitorMachine);
        return true;
    }

    /**
     * 修改machine
     *
     * @param machineName
     * @param machineIp
     * @param operateSystemType
     * @param desc
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean editMachine(Integer id,
                                      String machineName,
                                      String machineIp,
                                      Byte operateSystemType,
                                      String desc) {
        MonitorMachine monitorMachine = new MonitorMachine();
        Date now = new Date();
        monitorMachine.setId(id);
        monitorMachine.setMachineName(machineName);
        monitorMachine.setMachineIp(machineIp);
        monitorMachine.setOperateSystemType(operateSystemType);
        monitorMachine.setDesc(desc);
        monitorMachine.setUpdateTime(now);

        int count = monitorMachineExMapper.updateByPrimaryKeySelective(monitorMachine);
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean isExists(String machineName) {
        return selectOneByMachineName(machineName) == null ? false : true;
    }

    public boolean isExists(int machineId) {
        return selectOneByMachineId(machineId) == null ? false : true;
    }

}
