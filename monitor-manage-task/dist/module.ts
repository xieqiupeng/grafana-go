///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />

import {MetricsPanelCtrl, PanelCtrl} from 'app/plugins/sdk';
import _ from 'lodash';
import './css/module.css!';

// import './css/jqui.css!';

const panelDefaults = {
    serverHost: 'http://127.0.0.1:8080/'
};

class MonitorManageCtrl extends MetricsPanelCtrl {
    static templateUrl = 'partials/module.html';

    defaults = {};

    /** @ngInject **/
    constructor($scope, $injector, private $http, private uiSegmentSrv) {

        super($scope, $injector);
        // defaults configs
        _.defaultsDeep(this.panel, panelDefaults);
        this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
        // this.events.on('panel-initialized', this.onPanelInitalized.bind(this));
        this.events.on('panel-initialized', this.render.bind(this));
        this.searchAllAuthorizeProjects(panelDefaults.serverHost);
    }

    onPanelInitalized() {

    }
    allAuthorizeProject=[];
    allAuthorizeMachine=[];
    allAuthorizeMachineChecked=[];
    public onInitEditMode() {
        this.addEditorTab('Options', 'public/plugins/monitor-manage-panel/partials/option.html', 1);
    }

    //搜索所有授权的项目
    searchAllAuthorizeProjects = function (serverHost) {
        this.allAuthorizeProject=[];
        this.$http({
            url: serverHost + 'monitorProject/searchAllAuthorizeProjects',
            withCredentials: true,
            method: 'GET'
        }).then((rsp) => {
            if (rsp.data.resultCode == 0) {
                console.log("invoke searchAllAuthorizeProjects ok:", rsp.data.data);
                //设置列表内容
                this.allAuthorizeProject = rsp.data.data;
                // if(rsp.data.data.length>0){
                //
                // }
            } else {
                alert('获取所有授权的项目失败!具体原因：' + rsp.data.resultMsg);
            }
        }, err => {
            console.log("invoke searchAllAuthorizeProjects err:", err);
            alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
        });
    };

    changeServerHost(object) {
        // alert(this.serverHost);
        // console.log(object);
    }


    monitorManageController($scope, $http) {
        //查询参数
        $scope.taskName='';
        //列表内容
        $scope.taskArray = [];
        //分页参数
        $scope.total = 0; //总条数
        $scope.pages = 0; //总页面
        $scope.pageNum = 0; //当前页面
        $scope.pageSize = 5;//页面大小
        $scope.hasPreviousPage = false;//有前一页
        $scope.hasNextPage = false;//有后一页

        $scope.data = {
            current: 1 // 1代表查询，2代表编辑,3代表新增
        };
        $scope.actions = {
             setCurrent: function (param) {
                 $scope.data.current = param;
             }
        };
        $scope.formData={
            /************基本属性***********/
            taskName:'',
            dataSourceLog:'',
            template:"0",
            /************分隔符属性***********/
            isRegex:false,//是否正则
            isOrder:false,//是否有序
            separatorKeys:[],//分隔符数组
            separatorKeyIndex:0,//分隔符索引值
            /************结果属性***********/
            resultColumns:[],//结果列数组
            resultColumnIndex:0,//结果列索引值
            dataSourceLogSample:''
        };



        $scope.selectChangeProject=function (serverHost) {
            $scope.projectId=document.getElementById('projectId');
            $scope.projectId=($scope.projectId==null? "":$scope.projectId.value);
            if($scope.projectId!=null&&$scope.projectId!=''){
                $scope.searchAllAuthorizeMachines(serverHost,$scope.projectId);
            }else{
                this.ctrl.allAuthorizeMachine=[];
                this.ctrl.allAuthorizeMachineChecked=[];
            }
        }

        //搜索所有授权的机器
        $scope.searchAllAuthorizeMachines = function (serverHost,projectId) {

            $http({
                url: serverHost + 'monitorMachine/searchAllAuthorizeMachinesByProjectId?projectId='+projectId,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                if (rsp.data.resultCode == 0) {
                    console.log("invoke searchAllAuthorizeMachines ok:", rsp.data.data);
                    //设置列表内容
                    this.ctrl.allAuthorizeMachine=[];
                    this.ctrl.allAuthorizeMachineChecked=[];
                    this.ctrl.allAuthorizeMachine = rsp.data.data;
                    for(var i=0;i<this.ctrl.allAuthorizeMachine.length;i++){
                        this.ctrl.allAuthorizeMachineChecked.push(false);
                    }
                    // if(this.ctrl.allAuthorizeMachine.length>0){
                    //     // $scope.formData.machineId=this.ctrl.allAuthorizeMachine[0].id.toString();
                    //     // console.log('$scope.formData.machineId '+$scope.formData.machineId);
                    // }

                    // if(rsp.data.data.length>0){
                    //
                    // }
                } else {
                    alert('获取所有授权的机器失败!具体原因：' + rsp.data.resultMsg);
                }
            }, err => {
                console.log("invoke searchAllAuthorizeMachines err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        };



        //切换是否正则
        $scope.changeIsRegex=function () {
            if($scope.formData.isRegex=="false"){
                $scope.formData.isRegex="true";
            }else if($scope.formData.isRegex=="true"){
                $scope.formData.isRegex="false";
            }
        }

        //切换是否有序
        $scope.changeIsOrder=function () {
            if($scope.formData.isOrder=="false"){
                $scope.formData.isOrder="true";
            }else if($scope.formData.isOrder=="true"){
                $scope.formData.isOrder="false";
            }
        }


        //新增分隔符
        $scope.addSeparatorKey=function (separatorKeyObject) {
            if(separatorKeyObject==null){
                //添加空对象
                separatorKeyObject={separatorKeyIndex:'',separatorKey:''};
                separatorKeyObject.separatorKey="";
            }
            separatorKeyObject.separatorKeyIndex='separatorKeyIndex'+$scope.formData.separatorKeyIndex;
            $scope.formData.separatorKeyIndex++;
            $scope.formData.separatorKeys.push(separatorKeyObject);
        }


        //新增结果列
        $scope.addResultColumn=function (resultColumnObject) {
            if(resultColumnObject==null){
                //添加默认对象
                resultColumnObject={resultColumnIndex:'',columnExampleValue:'',columnSeq:'',columnName:'',columnType:'',format:'',tagOrValue:''};

                var biggestColumnSeq=-1;
                for(var i=0;i<$scope.formData.resultColumns.length;i++){
                    if(biggestColumnSeq<$scope.formData.resultColumns[i].columnSeq){
                        biggestColumnSeq=$scope.formData.resultColumns[i].columnSeq;
                    }
                }
                resultColumnObject.columnSeq=biggestColumnSeq+1;
                resultColumnObject.columnName="列名"+(biggestColumnSeq+1);
                resultColumnObject.columnType="string";
                resultColumnObject.format="";
                resultColumnObject.tagOrValue=false;
            }
            resultColumnObject.resultColumnIndex='resultColumnIndex'+$scope.formData.resultColumnIndex;
            $scope.formData.resultColumnIndex++;
            $scope.formData.resultColumns.push(resultColumnObject);
        }
        //删除结果列
        $scope.removeColumnResult=function(resultColumnIndex){
            var newResultColumns=new Array();
            for(var i=0;i<$scope.formData.resultColumns.length;i++){
                if($scope.formData.resultColumns[i].resultColumnIndex!=resultColumnIndex){
                    newResultColumns.push($scope.formData.resultColumns[i]);
                }
            }
            $scope.formData.resultColumns=newResultColumns;
        }
        //删除分隔符
        $scope.removeSeparatorKey=function(separatorKeyIndex){
            var newSeparatorKeys=new Array();
            for(var i=0;i<$scope.formData.separatorKeys.length;i++){
                if($scope.formData.separatorKeys[i].separatorKeyIndex!=separatorKeyIndex){
                    newSeparatorKeys.push($scope.formData.separatorKeys[i]);
                }
            }
            $scope.formData.separatorKeys=newSeparatorKeys;
        }
        //搜索功能
        $scope.searchFunction = function (serverHost) {
            $scope.taskName=document.getElementById('taskName');
            $scope.taskName=$scope.taskName.value;
            $scope.taskArray = [];
            var param = 'taskName=' + $scope.taskName+ "&pageNum=" + $scope.pageNum + "&pageSize=" + $scope.pageSize;
            $http({
                url: serverHost + 'monitorTask/searchTaskByTaskName' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {

                if (rsp.data.resultCode == 0) {
                    console.log("invoke searchFunction ok:", rsp.data.data);
                    //设置列表内容

                    for (var i = 0; i < rsp.data.data.list.length; i++) {
                        //处理状态
                        if (0 == rsp.data.data.list[i].status) {
                            rsp.data.data.list[i].status = '启动';
                        } else if (1 == rsp.data.data.list[i].status) {
                            rsp.data.data.list[i].status = '暂停';
                        }

                    }
                    $scope.taskArray = rsp.data.data.list;
                    //设置分页内容
                    $scope.pageNum = rsp.data.data.pageNum; //当前页面
                    $scope.total = rsp.data.data.total;//总条数
                    $scope.pages = rsp.data.data.pages; //总页面
                    $scope.hasPreviousPage = rsp.data.data.hasPreviousPage;//有前一页
                    $scope.hasNextPage = rsp.data.data.hasNextPage;//有后一页

                } else {
                    alert('解析失败!具体原因：' + rsp.data.resultMsg);
                }
            }, err => {
                console.log("invoke searchFunction err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        };

        //启动/暂停
        $scope.startOrPauseTaskFunction = function (serverHost, taskName, status) {
            var param = 'taskName=' + taskName + '&status=' + status;
            $http({
                url: serverHost + 'monitorTask/startOrPauseTask' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                console.log("invoke startOrPauseTask ok:", rsp.data.resultCode, rsp.data.resultMsg);
                if (rsp.data.resultCode == 0) {
                    //重新拉取监控任务
                    $scope.searchFunction(serverHost);
                } else {
                    alert('启动/暂停失败！具体原因：' + rsp.data.resultMsg + "。");
                }
            }, err => {
                console.log("invoke startOrPauseTask err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        };

        //数据清洗
        $scope.dataClean=function (serverHost){

            //任务名称
            var taskName=$scope.formData.taskName;
            if(taskName==null||taskName.trim()==''){
                alert('请填写任务名称!');
                return ;
            }
            if($scope.checkContainChineseCharacter(taskName)){
                alert('任务名称不能含有中文!');
                return ;
            }

            //数据源文件位置
            var dataSourceLog=$scope.formData.dataSourceLog;
            if(dataSourceLog==null||dataSourceLog.trim()==''){
                alert('请填写数据源文件位置!');
                return ;
            }

            //切割模板类型
            var template=$scope.formData.template;
            //是否为正则
            var isRegex=$scope.formData.isRegex;
            //是否为有序
            var isOrder=$scope.formData.isOrder;
            //请填写分隔符
            var separatorKeys=$scope.formData.separatorKeys;
            // for(var i=0;i<separatorKeys.length;i++){
            //     if(separatorKeys[i]==null||""==separatorKeys[i].trim()){
            //         alert("分隔符内容不能为空");
            //         return ;
            //     }
            // }
            console.log('separatorKeys '+JSON.stringify(separatorKeys));
            //请填写结果列
            var resultColumns=$scope.formData.resultColumns;

            for(var i=0;i<resultColumns.length;i++){
                if(resultColumns[i].columnSeq==null||resultColumns[i].columnName==null||""==resultColumns[i].columnName.trim()){
                    alert("结果列内容不能为空");
                    return ;
                }
            }
            //删除多余的字段--结果列
            for(var i=0;i<resultColumns.length;i++){
                delete resultColumns[i].resultColumnIndex;
            }


            //获得切割模板对象
            var cutTemplateObject=$scope.getCutTemplateObject(template,isOrder,isRegex,separatorKeys,resultColumns);
            //传输数据
            cutTemplateObject=JSON.stringify(cutTemplateObject);


            console.log('separatorKeys '+cutTemplateObject);

            $http({
                url: serverHost + "monitorTask/dataClean",
                data:"data="+$scope.formData.dataSourceLogSample+"&dataCleanRule="+cutTemplateObject,
                headers:{'Content-Type': 'application/x-www-form-urlencoded'},
                withCredentials: true,
                method: 'POST'
            }).then((rsp) => {
                if (rsp.data.resultCode == 0) {
                    //结果列数组
                    $scope.formData.resultColumns=[];
                    //结果列索引值
                    $scope.formData.resultColumnIndex=0;

                    for(var i=0;i< rsp.data.data.length;i++){
                        if(rsp.data.data[i].tagOrValue==0){
                            rsp.data.data[i].tagOrValue=true;
                        }else if(rsp.data.data[i].tagOrValue==1){
                            rsp.data.data[i].tagOrValue=false;
                        }else{
                            alert('tagOrValue发生异常！');
                        }
                        $scope.addResultColumn(rsp.data.data[i]);
                    }
                } else {
                    alert('解析失败!具体原因：' + rsp.data.resultMsg);
                }
            }, err => {
                console.log("invoke searchFunction err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });



        }



        //校验任务名称是否含有中文
        $scope.checkContainChineseCharacter=function(taskName){
            for(var i = 0;i < taskName.length; i++)
            {
                if(taskName.charCodeAt(i) > 255) //如果是汉字
                    return true
            }
            return false;
        }

        //切换tab时,提示确定退出
        $scope.confirmChangeTab=function(serverHost){
            if($scope.data.current!=1){
                var tabName=($scope.data.current==2?'编辑':'新建');
                var result = confirm('确定退出'+tabName+'页面？');
                if(result){
                    $scope.actions.setCurrent(1);
                }
            }
        }
        //保存或者更新
        $scope.saveOrUpdate=function(serverHost){

            //任务名称
            var taskName=$scope.formData.taskName;
            if(taskName==null||taskName.trim()==''){
                alert('请填写任务名称!');
                return ;
            }
            if($scope.checkContainChineseCharacter(taskName)){
                alert('任务名称不能含有中文!');
                return ;
            }

            var projectId=$scope.formData.projectId;
            console.log(projectId);
            if(projectId==null||projectId==""){
                alert('请选择项目!');
                return ;
            }

            //处理监控机器Id
            if(this.ctrl.allAuthorizeMachineChecked.length>0){
                var allAreFalse=true;
                var allMonitorMachineIdsStr="";
                for(var i=0;i<this.ctrl.allAuthorizeMachineChecked.length;i++){
                    if(this.ctrl.allAuthorizeMachineChecked[i]==true){
                        allAreFalse=false;
                        allMonitorMachineIdsStr+=this.ctrl.allAuthorizeMachine[i].id+",";
                    }
                }
                if(true==allAreFalse){
                    alert('请选择监测机器！');
                    return ;
                }
                if(allMonitorMachineIdsStr.length>0)
                    allMonitorMachineIdsStr=allMonitorMachineIdsStr.substr(0,allMonitorMachineIdsStr.length-1);
            }

            //数据源文件位置
            var dataSourceLog=$scope.formData.dataSourceLog;
            if(dataSourceLog==null||dataSourceLog.trim()==''){
                alert('请填写数据源文件位置!');
                return ;
            }


            //切割模板类型
            var template=$scope.formData.template;
            //是否为正则
            var isRegex=$scope.formData.isRegex;
            console.log('isRegex '+$scope.formData.isRegex);
            //是否为有序
            var isOrder=$scope.formData.isOrder;
            console.log('isOrder '+$scope.formData.isOrder);
            //请填写分隔符
            var separatorKeys=$scope.formData.separatorKeys;
            // for(var i=0;i<separatorKeys.length;i++){
            //     if(separatorKeys[i]==null||""==separatorKeys[i].trim()){
            //         alert("分隔符内容不能为空");
            //         return ;
            //     }
            // }

            //请填写结果列
            var resultColumns=$scope.formData.resultColumns;

            for(var i=0;i<resultColumns.length;i++){
                if(resultColumns[i].columnSeq==null||resultColumns[i].columnName==null||""==resultColumns[i].columnName.trim()){
                    alert("结果列内容不能为空");
                    return ;
                }
            }


            //删除多余的字段--结果列
            for(var i=0;i<resultColumns.length;i++){
                delete resultColumns[i].resultColumnIndex;
            }


            //获得切割模板对象
            var cutTemplateObject=$scope.getCutTemplateObject(template,isOrder,isRegex,separatorKeys,resultColumns);

            var saveOrUpdateContextPath='';
            if($scope.data.current==2){
                saveOrUpdateContextPath='monitorTask/editTask';
            }else if($scope.data.current==3){
                saveOrUpdateContextPath='monitorTask/addTask';
            }

            $http({
                url: serverHost + saveOrUpdateContextPath ,
                withCredentials: true,
                method: 'POST',
                data:{
                    taskName:taskName,
                    machineId:allMonitorMachineIdsStr,
                    projectId:projectId,
                    dataSourceLog:dataSourceLog,
                    cutTemplate:cutTemplateObject
                }
            }).then((rsp) => {
                console.log("invoke "+saveOrUpdateContextPath+" ok:", rsp.data.resultCode, rsp.data.resultMsg);

                if (rsp.data.resultCode == 0) {
                    alert('保存成功！');
                    //重新拉取监控任务
                    $scope.searchFunction(serverHost);
                    //跳转到查询tab
                    $scope.actions.setCurrent(1);
                } else {
                    alert('保存失败！具体原因：' + rsp.data.resultMsg + "。");
                }

            }, err => {
                console.log("invoke "+saveOrUpdateContextPath+" err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        }


        $scope.getCutTemplateObject=function(template,isOrder,isRegex,separatorKeys,resultColumns) {
            //拼接切割模板
            var cutTemplate={template:'',separator:{},resultColumns:[]};
            cutTemplate.template=template;
            var separatorObject={isOrder:false,isRegex:false,separatorKeys:{}};
            separatorObject.isOrder=isOrder;
            separatorObject.isRegex=isRegex;

            var separatorKeysArray=new Array();
            console.log('aa '+JSON.stringify(separatorKeys));
            for(var i=0;i<separatorKeys.length;i++){
                separatorKeysArray.push(separatorKeys[i].separatorKey);
            }
            console.log('bb '+JSON.stringify(separatorKeysArray));
            separatorObject.separatorKeys=separatorKeysArray;
            cutTemplate.separator=separatorObject;

            resultColumns=$scope.clone(resultColumns);
            //删除多余的字段--结果列
            for(var i=0;i<resultColumns.length;i++){
                delete resultColumns[i].resultColumnIndex;
            }
            for(var i=0;i<resultColumns.length;i++){
                if(true==resultColumns[i].tagOrValue){
                    resultColumns[i].tagOrValue=0;
                }else if(false==resultColumns[i].tagOrValue){
                    resultColumns[i].tagOrValue=1;
                }else{
                    alert('tagOrValue发生异常！');
                }
            }
            cutTemplate.resultColumns=resultColumns;
            return cutTemplate;
        }


        $scope.clone=function (obj) {
            // Handle the 3 simple types, and null or undefined
            if (null == obj || "object" != typeof obj) return obj;

            // Handle Date
            var copy;
            if (obj instanceof Date) {
                copy = new Date();
                copy.setTime(obj.getTime());
                return copy;
            }

            // Handle Array
            if (obj instanceof Array) {
                copy = [];
                for (var i = 0, len = obj.length; i < len; ++i) {
                    copy[i] = $scope.clone(obj[i]);
                }
                return copy;
            }

            // Handle Object
            if (obj instanceof Object) {
                copy = {};
                for (var attr in obj) {
                    if (obj.hasOwnProperty(attr)) copy[attr] = $scope.clone(obj[attr]);
                }
                return copy;
            }

            throw new Error("Unable to copy obj! Its type isn't supported.");
        }


        //清空新建or编辑Tab页面
        $scope.clearNewOrEditMonitorTaskTab=function(){

            /********************************基本属性************************************/
            //任务名称
            $scope.formData.taskName="";
            //机器Id,多个用逗号分隔
            $scope.formData.machineId="";
            //数据源文件位置
            $scope.formData.dataSourceLog="";
            //设置切割模板类型 切割模板类型-默认普通文本类型
            $scope.formData.template="0";


            /********************************数据清洗规则************************************/
            //是否为正则
            $scope.formData.isRegex=false;
            //是否为有序
            $scope.formData.isOrder=false;
            //分隔符数组
            $scope.formData.separatorKeys=[];
            //分隔符索引值
            $scope.formData.separatorKeyIndex=0;

            /********************************结果属性************************************/
            //结果列数组
            $scope.formData.resultColumns=[];
            //结果列索引值
            $scope.formData.resultColumnIndex=0;
            //数据源样例
            $scope.formData.dataSourceLogSample='';

        }

        $scope.selectChangeColumnType=function (resultColumnObject){
            if(resultColumnObject.columnType=='date'){
                resultColumnObject.format="yyyy-MM-dd HH:mm:ss";
            }else{
                resultColumnObject.format="";
            }
        }

        $scope.showAddMonitorTaskTab=function () {
            //清空新增Tab页面
            $scope.clearNewOrEditMonitorTaskTab();
            //跳转到新增页面
            $scope.actions.setCurrent(3);
        }

        $scope.showEditMonitorTaskTab=function (serverHost,taskName) {
            //清空编辑Tab页面
            $scope.clearNewOrEditMonitorTaskTab();
            var param = 'taskName=' + taskName;
            $http({
                url: serverHost + 'monitorTask/getTaskByTaskName' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                console.log("invoke getTaskByTaskName ok:", rsp.data.resultCode, rsp.data.resultMsg,rsp.data.data);

                if (rsp.data.resultCode == 0) {

                    /********************************基本属性************************************/
                    rsp.data.data.cutTemplate=JSON.parse(rsp.data.data.cutTemplate);
                    //任务名称
                    $scope.formData.taskName=rsp.data.data.taskName;
                    //数据源Ip
                    $scope.formData.machineId=rsp.data.data.machineId;
                    //数据源文件位置
                    $scope.formData.dataSourceLog=rsp.data.data.dataSourceLog;

                    //设置切割模板类型
                    $scope.formData.template=rsp.data.data.cutTemplate.template;

                    /********************************分隔符属性************************************/
                    //是否为正则
                    $scope.formData.isRegex=rsp.data.data.cutTemplate.separator.isRegex;
                    //是否为有序
                    $scope.formData.isOrder=rsp.data.data.cutTemplate.separator.isOrder;
                    //分隔符数组
                    var separatorKeys=rsp.data.data.cutTemplate.separator.separatorKeys;
                    //新增分隔符
                    for(var i=0;i<separatorKeys.length;i++){
                        var separatorKeyObject={separatorKeyIndex:'',separatorKey:''};
                        separatorKeyObject.separatorKey=separatorKeys[i];
                        $scope.addSeparatorKey(separatorKeyObject);
                    }
                    for(var i=0;i<$scope.formData.separatorKeys.length;i++){
                        console.log($scope.formData.separatorKeys[i]);
                    }


                    /********************************结果列属性************************************/
                    //结果列数组
                    var resultColumns=rsp.data.data.cutTemplate.resultColumns;
                    //新增结果列
                    for(var i=0;i<resultColumns.length;i++){
                        var resultColumnObject={resultColumnIndex:'',columnSeq:'',columnName:'',columnType:'',format:'',tagOrValue:false};
                        resultColumnObject.columnSeq=resultColumns[i].columnSeq;
                        resultColumnObject.columnName=resultColumns[i].columnName;
                        resultColumnObject.columnType=resultColumns[i].columnType;
                        resultColumnObject.format=resultColumns[i].format;
                        if(resultColumns[i].tagOrValue==0){
                            resultColumnObject.tagOrValue=true;
                        }else if(resultColumns[i].tagOrValue==1){
                            resultColumnObject.tagOrValue=false;
                        }else{
                            alert('tagOrValue发生异常！');
                        }

                        $scope.addResultColumn(resultColumnObject);
                    }
                    //跳转到编辑页面
                    $scope.actions.setCurrent(2);
                } else {
                    alert('获取任务记录失败！具体原因：' + rsp.data.resultMsg + "。");
                }
            }, err => {
                console.log("invoke getTaskByTaskName err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        }


        //删除
        $scope.deleteTaskFunction = function (serverHost, taskName) {

            if (!confirm("确定要删除" + taskName + "吗？")) {
                return;
            }

            var param = 'taskName=' + taskName;
            $http({
                url: serverHost + 'monitorTask/deleteTask' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                console.log("invoke deleteTask ok:", rsp.data.resultCode, rsp.data.resultMsg);
                if (rsp.data.resultCode == 0) {
                    //重新拉取监控任务
                    $scope.searchFunction(serverHost);
                } else {
                    alert('删除失败！具体原因：' + rsp.data.resultMsg + "。");
                }

            }, err => {
                console.log("invoke deleteTask err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        };

        //下一页
        $scope.nextPageFunction = function (serverHost) {
            $scope.pageNum += 1;
            //重新拉取监控任务
            $scope.searchFunction(serverHost);
        };

        //上一页
        $scope.lastPageFunction = function (serverHost) {
            $scope.pageNum -= 1;
            //重新拉取监控任务
            $scope.searchFunction(serverHost);
        };
    }
}

export {
    MonitorManageCtrl as PanelCtrl
};


