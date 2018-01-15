package com.creditease.monitor.service;

import com.alibaba.fastjson.JSON;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.creditease.monitor.constant.MonitorConstant;
import com.creditease.monitor.etcd.entity.MonitorNoteDataEntity;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorApplication;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorMachine;
import com.creditease.monitor.mybatis.sqllite.grafana.po.MonitorTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 监控应用、任务针对ETCD的操作
 */
@Service
public class MonitorEtcdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorEtcdService.class);

    @Autowired
    private Client etcdClient;

    @Autowired
    private MonitorMachineService monitorMachineService;

//    private static final String MONITOR_TASK_DIR = "/monitor";
    private static final String MONITOR_DIR = "/testmonitor";
    private static final String PROJECT_PREFFIX="/projectId";
    private static final String TASK_PREFFIX="_taskName";
    private static final String APPLICATION_HOME = "_applicationHome";

    private static final String ETCD_DATA_TYPE = "log";



    //更新application_home文件
    public boolean upSertApplicationHome(MonitorApplication monitorApplication) {
        if (monitorApplication != null) {

            //获取机器Ip
            Integer machineId = monitorApplication.getMachineId();
            Integer projectId = monitorApplication.getProjectId();
            checkNull(String.valueOf(machineId));
            checkNull(String.valueOf(projectId));
            MonitorMachine monitorMachine = monitorMachineService.selectByMachineIdAndProjectId (machineId, projectId);

            /*************************************需要新增的service**************************/

            try {

                List<String>needParamList=new ArrayList<>();
                String[] paramArray = monitorApplication.getApplicationDetailParam().split(MonitorConstant.comma);
                for(int i=0;i<paramArray.length;i++){
                    needParamList.add(monitorMachine.getMachineIp()+MonitorConstant.colon+paramArray[i]);
                }

                /*************************************检查service是否存在**************************/
                MonitorNoteDataEntity applicationHomeDataEntity = selectApplicationHome(monitorApplication.getProjectId());
                List<MonitorNoteDataEntity.MonitorService> applicationHomeServices = applicationHomeDataEntity.getServices();

                boolean hasAddService=false;
                for (MonitorNoteDataEntity.MonitorService applicationMonitorService:applicationHomeServices){
                    if(applicationMonitorService.getHost().equals(monitorMachine.getMachineIp())){
                        List<MonitorNoteDataEntity.ServerTypeParam> applicationServerTypeParams = applicationMonitorService.getServerTypeParams();
                        for (MonitorNoteDataEntity.ServerTypeParam applicationServerTypeParam:applicationServerTypeParams){
                            int applicationType = applicationServerTypeParam.getType();
                            //新增的一个服务只有一个serverTypeParam元素
                            String beforeMergeStr=JSON.toJSONString(applicationHomeDataEntity);
                            //同一个ip的相同应用
                            if (applicationType==monitorApplication.getApplicationType()){
                                for(String needParam:needParamList){
                                    if (!applicationServerTypeParam.getParam().contains(needParam)){
                                        applicationServerTypeParam.getParam().add(needParam);
                                    }
                                }
                                LOGGER.info("etcd含有ip={}的应用,所以合并此ip的应用,合并前的内容为{},合并后的内容为{}",monitorMachine.getMachineIp(),beforeMergeStr,JSON.toJSONString(applicationHomeDataEntity));
                                hasAddService=true;
                                break;
                            }
                        }
                        if(hasAddService==false){
                            //同一个ip的不同应用
                            MonitorNoteDataEntity.ServerTypeParam needServerTypeParam=new MonitorNoteDataEntity.ServerTypeParam();
                            needServerTypeParam.setParam(needParamList);
                            needServerTypeParam.setType(monitorApplication.getApplicationType());
                            applicationServerTypeParams.add(needServerTypeParam);
                            hasAddService=true;
                        }
                        break;
                    }
                }
                if(false==hasAddService){
                    //向applicationHome文件新增service记录
                    MonitorNoteDataEntity.MonitorService needAddService=new MonitorNoteDataEntity.MonitorService();
                    MonitorNoteDataEntity.ServerTypeParam needServerTypeParam=new MonitorNoteDataEntity.ServerTypeParam();
                    needServerTypeParam.setParam(needParamList);
                    needServerTypeParam.setType(monitorApplication.getApplicationType());
                    needAddService.setHost(monitorMachine.getMachineIp());
                    needAddService.setServerTypeParams(new ArrayList<MonitorNoteDataEntity.ServerTypeParam>(){{
                        add(needServerTypeParam);
                    }});
                    applicationHomeDataEntity.getServices().add(needAddService);
                    LOGGER.info("etcd不含有此ip={}的应用,所以直接添加此ip的应用",needAddService.getHost());
                }
                String str = JSON.toJSONString(applicationHomeDataEntity);
                setValue(monitorApplication.getProjectId(),APPLICATION_HOME,str);
                return true;
            } catch (Exception e) {
                LOGGER.error("upSertMonitorApplication error monitorApplication={},errorMsg",
                        JSON.toJSONString(monitorApplication),
                        e.getMessage());
            }
        }
        return false;
    }

    //初始化applicationHome文件,创建一个空的文件
    public boolean initApplicationHome(Integer projectId) throws Exception {
        MonitorNoteDataEntity applicationHomeDataEntity=new MonitorNoteDataEntity();
        applicationHomeDataEntity.setCleanRule(" ");
        applicationHomeDataEntity.setPath(new ArrayList<>());
        applicationHomeDataEntity.setServices(new ArrayList<>());
        applicationHomeDataEntity.setType(ETCD_DATA_TYPE);
        String applicationHomeDataEntityStr = JSON.toJSONString(applicationHomeDataEntity);
        return setValue(projectId,APPLICATION_HOME,applicationHomeDataEntityStr);
    }

    //创建监控任务
    public boolean upSertMonitorTask(MonitorTask monitorTask) {
        if (monitorTask != null) {

            //获取切割模板
            String cutTemplate = monitorTask.getCutTemplate();
            checkNull(cutTemplate);

            //获取机器Ip
            String machineId = monitorTask.getMachineId();
            checkNull(machineId);
            String[] machineIdArray = machineId.split(MonitorConstant.comma);
            LinkedHashSet<String> ips = new LinkedHashSet<>();
            for(int i=0;i<machineIdArray.length;i++){
                MonitorMachine monitorMachine = monitorMachineService.selectByMachineIdAndProjectId (Integer.parseInt(machineIdArray[i]), monitorTask.getProjectId());
                ips.add(monitorMachine.getMachineIp());
            }

            //获取日志位置
            String dataSourceLog = monitorTask.getDataSourceLog();
            checkNull(dataSourceLog);
            String[] pathArr = dataSourceLog.split(MonitorConstant.comma);

            //存储到etcd的实体类
            MonitorNoteDataEntity taskNoteDataEntity = new MonitorNoteDataEntity();

            //处理应用,首先从APPLICATION_HOME文件中拷贝,如果APPLICATION_HOME文件不存在,则首先创建APPLICATION_HOME文件，
            // APPLICATION_HOME文件位置:/MONITOR_DIR/projectId/APPLICATION_HOME。
            // 最后创建task文件,task文件路径/MONITOR_DIR/projectId/taskname
            try {
                MonitorNoteDataEntity applicationHomeDataEntity = selectApplicationHome(monitorTask.getProjectId());
                List<MonitorNoteDataEntity.MonitorService> needCopyServices = new ArrayList<>();
                List<MonitorNoteDataEntity.MonitorService> applicationHomeServices = applicationHomeDataEntity.getServices();
                //从applicationHome文件拷贝service记录
                for(String ip:ips){
                    for (int j=0;j<applicationHomeServices.size();j++){
                        MonitorNoteDataEntity.MonitorService applicationHomeMonitorService = applicationHomeServices.get(j);
                        if(applicationHomeMonitorService.getHost().equals(ip)){
                            needCopyServices.add(applicationHomeMonitorService);
                        }
                    }
                }
                taskNoteDataEntity.setType(ETCD_DATA_TYPE);
                taskNoteDataEntity.setCleanRule(cutTemplate);
                taskNoteDataEntity.setPath(Arrays.asList(pathArr));
                taskNoteDataEntity.setServices(needCopyServices);

                return setValue(monitorTask.getProjectId(),monitorTask.getTaskName(),JSON.toJSONString(taskNoteDataEntity));
            } catch (Exception e) {
                LOGGER.error("upSertMonitorTask error monitorTask={},errorMsg",
                        JSON.toJSONString(taskNoteDataEntity),
                        e.getMessage());
            }
        }
        return false;
    }

    public boolean setValue(Integer projectId,String key,String value) {
        String ectdKey = checkAndGetTaskEtcdKey(projectId,key);
        try {
            etcdClient.getKVClient()
                    .put(ByteSequence.fromString(ectdKey),
                            ByteSequence.fromString(value))
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    public boolean deleteApplicationHome(Integer projectId) {
        String ectdKey = checkAndGetApplicationHomeEtcdKey(projectId);
        try {
            etcdClient.getKVClient()
                    .delete(ByteSequence.fromString(ectdKey))
                    .get();
        } catch (InterruptedException | ExecutionException e1) {
            LOGGER.info("delete key={} error,msg={}", ectdKey, e1.getMessage());
            return false;
        }
        return true;
    }



    public boolean deleteTask(Integer projectId,String taskName) {
        String ectdKey = checkAndGetTaskEtcdKey(projectId,taskName);
        try {
            etcdClient.getKVClient()
                    .delete(ByteSequence.fromString(ectdKey))
                    .get();
        } catch (InterruptedException | ExecutionException e1) {
            LOGGER.info("delete key={} error,msg={}", ectdKey, e1.getMessage());
            return false;
        }
        return true;
    }


    public MonitorNoteDataEntity selectApplicationHome(Integer projectId) throws Exception {
        String ectdKey = checkAndGetApplicationHomeEtcdKey(projectId);
        if (StringUtils.isNotBlank(ectdKey)) {
            GetResponse response = etcdClient.getKVClient()
                    .get(ByteSequence.fromString(ectdKey))
                    .get();
            String nodeValue = response.getKvs()
                    .get(0)
                    .getValue()
                    .toStringUtf8();
            if (StringUtils.isNotBlank(nodeValue)) {
                return JSON.parseObject(nodeValue, MonitorNoteDataEntity.class);
            }
        }
        return null;
    }


    private void checkNull(Object str) {
        if (str==null) {
            throw new NullPointerException();
        }
        if (str instanceof String){
            if (StringUtils.isBlank((String)str)) {
                throw new NullPointerException();
            }
        }
    }

    private String checkAndGetTaskEtcdKey(Integer projectId, String taskName) {
        checkNull(projectId);
        checkNull(taskName);
        StringBuffer buffer = new StringBuffer(MONITOR_DIR);
        buffer.append(PROJECT_PREFFIX);
        buffer.append(projectId);
        buffer.append(TASK_PREFFIX);
        buffer.append(taskName);
        return buffer.toString();
    }

    private String checkAndGetApplicationHomeEtcdKey(Integer projectId) {
        checkNull(projectId);
        StringBuffer buffer = new StringBuffer(MONITOR_DIR);
        buffer.append(PROJECT_PREFFIX);
        buffer.append(projectId);
        buffer.append(APPLICATION_HOME);
        return buffer.toString();
    }


    public static void main(String[] args) {

//        MonitorTaskEtcdService monitorTaskEtcdService=new MonitorTaskEtcdService();
//        MonitorTask monitorTask=new MonitorTask();
//        monitorTask.setTaskName("newTaskName");
//        monitorTask.setDataSourceLog("a.txt");
//        monitorTask.setCutTemplate("CutTemplate");
//        if (monitorTask != null) {
//            String key = monitorTaskEtcdService.checkAndGetTaskEtcdKey(monitorTask.getTaskName()); // key:  /monitor/taskName
//            String cutTemplate = monitorTask.getCutTemplate();
//            String dataSourceServerIp ="127.0.0.1,127.0.0.2" ;
//            String dataSourceLog = monitorTask.getDataSourceLog();
//            monitorTaskEtcdService.checkNull(cutTemplate);
//            monitorTaskEtcdService.checkNull(dataSourceServerIp);
//            monitorTaskEtcdService.checkNull(dataSourceLog);
//            MonitorNoteDataEntity noteDataEntity = new MonitorNoteDataEntity();
//            noteDataEntity.setType("log");
//            noteDataEntity.setCleanRule(cutTemplate);
//            String[] pathArr = dataSourceLog.split(MonitorTaskConstant.comma);
//            noteDataEntity.setPath(Arrays.asList(pathArr));
//            String[] dataSourceServerIpArr = dataSourceServerIp.split(MonitorTaskConstant.comma);
//            LinkedHashSet<String> ips = new LinkedHashSet<>();
//            for (String ip : dataSourceServerIpArr) {
//                ips.add(ip);
//            }//ips 存储127.0.0.1,127.0.0.2
//            //存储HOST,类型，对应IP
//            Map<String, Map<Integer, List<Integer>>> map = new HashMap<>();
//            Map<String, List<Integer>> ipPorts = new HashMap<>();
//            ips.forEach(ip -> {
//                map.put(ip, new HashMap<>());
//                //map key1=127.0.0.1 value1=Map<Integer, List<Integer>>
//                //map key2=127.0.0.2 value2=Map<Integer, List<Integer>>
//                ipPorts.put(ip, new ArrayList<>());
//                //ipPorts key1=127.0.0.1 value1=List<Integer>
//                //ipPorts key2=127.0.0.2 value2=List<Integer>
//            });
//            //以上处理业务逻辑
//
//            //以下处理应用监控
//            String tomcatServerHost = "127.0.0.1:80,127.0.0.1:81";
////            if (monitorTask.getIsMonitorTomcatServer() != null
////                    && MonitorTaskConstant.MonitorTomcatServer.YES == monitorTask.getIsMonitorTomcatServer()
////                    && StringUtils.isNotBlank(tomcatServerHost))
//
//            if (StringUtils.isNotBlank(tomcatServerHost))
//            {
//                String[] ipPortsArray = tomcatServerHost.split(MonitorTaskConstant.comma);
//                for (String ipPortStr : ipPortsArray) {
//                    String[] ipPort = ipPortStr.split(MonitorTaskConstant.colon);
//                    if (ipPort.length > 1) {
//                        String ip = ipPort[0];
//                        if (ipPorts.containsKey(ip)) {
//                            int port = Integer.parseInt(ipPort[1]);
//                            List<Integer> ports = ipPorts.get(ip);
//                            if (!ports.contains(port)) {
//                                List<Integer> list = map.get(ip).get(MonitorTaskConstant.MonitorServerType.tomcatServer);
//                                if (list == null) {
//                                    list = new ArrayList<>();
//                                    map.get(ip).put(MonitorTaskConstant.MonitorServerType.tomcatServer, list);
//                                }
//                                list.add(port);
//                                ports.add(port);
//                            } else {
//                                LOGGER.info("host={},port={}重复", ip, port);
//                            }
//                        }
//                    }
//                }
//            }
//            if (map.isEmpty()) {
//                LOGGER.info("监控节点没有host");
//                //return false;
//            }
//            //
//            List<MonitorNoteDataEntity.MonitorService> monitorServices = new ArrayList<>();
//            for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
//                String host = it.next();
//                List<MonitorNoteDataEntity.ServerTypeParam> serverTypePorts = new ArrayList<>();
//                Map<Integer, List<Integer>> listMap = map.get(host);
//                for (Iterator<Integer> ite = listMap.keySet().iterator(); ite.hasNext(); ) {
//                    Integer type = ite.next();
//                    List<Integer> ports = listMap.get(type);
//                    List<String> params = new ArrayList<>();
//                    ports.forEach(port -> {
//                        StringBuffer buffer = new StringBuffer(host);
//                        buffer.append(MonitorTaskConstant.colon);
//                        buffer.append(port);
//                        params.add(buffer.toString());
//                    });
//                    MonitorNoteDataEntity.ServerTypeParam serverTypePort = new MonitorNoteDataEntity.ServerTypeParam();
//                    serverTypePort.setType(type);
//                    serverTypePort.setParam(params);
//                    serverTypePorts.add(serverTypePort);
//                }
//                MonitorNoteDataEntity.MonitorService monitorService = new MonitorNoteDataEntity.MonitorService();
//                monitorService.setHost(host);
//                monitorService.setServerTypeParams(serverTypePorts);
//                monitorServices.add(monitorService);
//            }
//            noteDataEntity.setServices(monitorServices);
//            //
//            String str = JSON.toJSONString(noteDataEntity);
//            System.out.println(key);
//            System.out.println(str);
            //
//            try {
//                etcdClient.getKVClient()
//                        .put(ByteSequence.fromString(key),
//                                ByteSequence.fromString(str))
//                        .get();
//                return true;
//            } catch (InterruptedException | ExecutionException e) {
//                LOGGER.error("upSertMonitorTask error monitorTask={},errorMsg",
//                        JSON.toJSONString(noteDataEntity),
//                        e.getMessage());
//            }

  //      }
    }


}
