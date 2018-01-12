package com.creditease.monitor.service;

import com.creditease.monitor.mybatis.sqllite.grafana.mapper.ex.MonitorProjectExMapper;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorProject;
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
 * @Date: created on 2018/01/04,at 10:54
 * @Modified By:  zhixinsong2
 * @Description:
 * @Other: All Copyright @ CreditEase
 */
@Service
public class MonitorProjectService {
    private static Logger logger = LoggerFactory.getLogger(MonitorProjectService.class);
    @Autowired
    private MonitorProjectExMapper monitorProjectExMapper;

    @Autowired
    private MonitorEtcdService monitorApplicationEtcdService;

    /**
     * 根据项目名称模糊查找
     *
     * @param projectName
     * @return
     */
    public List selectByProjectName(String projectName, Integer pageNum, Integer pageSize) {
        //设置参数
        if (projectName == null) {
            projectName = "";
        }
        projectName = "%" + projectName + "%";
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //执行查询
        List<MonitorProject> monitorProjectsList = monitorProjectExMapper.selectByProjectName(projectName);

        return monitorProjectsList;
    }



    /**
     * 搜索所有授权的项目
     * @return
     */
    public List selectAllAuthorizeProjects() {

        //执行查询
        List<MonitorProject> monitorProjectsList = monitorProjectExMapper.selectAllAuthorizeProjects();

        return monitorProjectsList;
    }



    /**
     * 删除Project
     *
     * @param projectId
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean deleteProject(int projectId) {
        //删除当前监控项目
        monitorProjectExMapper.deleteByPrimaryKey(projectId);
        try {
            return monitorApplicationEtcdService.deleteApplicationHome(projectId);
        } catch (Exception e) {
            logger.error("删除projectId为{}的ApplicationHome失败,失败原因：{}",projectId,e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public MonitorProject selectOneByProjectId(int projectId) {
        return monitorProjectExMapper.selectByPrimaryKey(projectId);
    }

    /**
     * 添加任务
     *
     * @param projectName
     * @param desc
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean addProject(String projectName,
                           String desc) {
        MonitorProject monitorProject = new MonitorProject();
        monitorProject.setProjectName(projectName);
        monitorProject.setProjectDesc(desc);
        Date now = new Date();
        monitorProject.setCreateTime(now);
        monitorProject.setUpdateTime(now);
        monitorProjectExMapper.insertSelective(monitorProject);
        try {
            return monitorApplicationEtcdService.initApplicationHome(monitorProject.getId());
        } catch (Exception e) {
            logger.error("初始化projectId为{}的ApplicationHome失败",monitorProject.getId());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 修改project
     *
     * @param id
     * @param projectDesc
     * @return
     */
    @Transactional(rollbackFor = {})
    public boolean editProject(Integer id,
                            String projectDesc
                            ) {
        MonitorProject monitorProject = new MonitorProject();
        Date now = new Date();
        monitorProject.setId(id);
        monitorProject.setProjectDesc(projectDesc);
        monitorProject.setUpdateTime(now);
        int count = monitorProjectExMapper.updateByPrimaryKeySelective(monitorProject);
        if (count > 0) {
            return true;
        }
        return false;
    }

    public MonitorProject selectOneByProjectName(String projectName) {
        return monitorProjectExMapper.selectOneByProjectName(projectName);
    }

    public boolean isExists(String projectName) {
        return selectOneByProjectName(projectName) == null ? false : true;
    }

    public boolean isExists(int projectId) {
        return selectOneByProjectId(projectId) == null ? false : true;
    }

}
