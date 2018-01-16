///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />

import {MetricsPanelCtrl, PanelCtrl} from 'app/plugins/sdk';
import _ from 'lodash';
import './css/module.css!';

// import './css/jqui.css!';

const panelDefaults = {

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
        this.panel.title='应用管理';
        this.searchAllAuthorizeProjects(this.panel.serverHost);
    }


    onPanelInitalized() {

    }

    public onInitEditMode() {
        this.addEditorTab('Options', 'public/plugins/monitor-manage-application-panel/partials/option.html', 1);
    }

    changeServerHost(object) {
        // alert(this.serverHost);
        // console.log(object);
    }

    allAuthorizeMachine=[];
    allAuthorizeProject=[];
    //搜索所有授权的机器
    searchAllAuthorizeMachines = function (serverHost,projectId) {
        this.allAuthorizeMachine=[];
        this.$http({
            url: serverHost + 'monitorMachine/searchAllAuthorizeMachinesByProjectId?projectId='+projectId,
            withCredentials: true,
            method: 'GET'
        }).then((rsp) => {
            if (rsp.data.resultCode == 0) {
                console.log("invoke searchAllAuthorizeMachines ok:", rsp.data.data);
                //设置列表内容
                this.allAuthorizeMachine = rsp.data.data;


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




    monitorManageController($scope, $http) {
        //查询参数
        $scope.applicationName='';
        //查询参数
        $scope.machineId='';
        //查询参数
        $scope.projectId='';
        //列表内容
        $scope.applicationArray = [];
        //分页参数
        $scope.total = 0; //总条数
        $scope.pages = 0; //总页面
        $scope.pageNum = 0; //当前页面
        $scope.pageSize = 5;//页面大小
        $scope.hasPreviousPage = false;//有前一页
        $scope.hasNextPage = false;//有后一页

        $scope.data = {
            current: 1 // 1代表查询，2代表编辑,3代表新建
        };
        $scope.actions = {
             setCurrent: function (param) {
                 $scope.data.current = param;
             }
        };
        $scope.formData={
            /************基本属性***********/
            id:null,
            applicationName:'',
            applicationType:"0",
            applicationDetailParam:'',
            machineId:'',
            projectId:'',
            applicationDesc:''
        };

        $scope.selectChangeProject=function (serverHost) {
            $scope.projectId=document.getElementById('projectId');
            $scope.projectId=($scope.projectId==null? "":$scope.projectId.value);
            //项目下拉列表发生变化时,1、机器id设置为空，2、机器下拉列表设置为空。
            $scope.machineId=''
            this.ctrl.allAuthorizeMachine=[];

            if($scope.projectId!=null&&$scope.projectId!=''){
                this.ctrl.searchAllAuthorizeMachines(serverHost,$scope.projectId);
            }else{
                // this.ctrl.allAuthorizeMachine=[];
            }
        }

        //搜索功能
        $scope.searchFunction = function (serverHost) {
            $scope.applicationName=document.getElementById('applicationName');
            $scope.applicationName=($scope.applicationName==null? "":$scope.applicationName.value);

            $scope.projectId=document.getElementById('projectId');
            $scope.projectId=($scope.projectId==null? "":$scope.projectId.value);

            $scope.machineId=document.getElementById('machineId');
            $scope.machineId=($scope.machineId==null? "":$scope.machineId.value);

            if($scope.projectId==null || $scope.projectId==''){
                $scope.machineId='';
                this.ctrl.allAuthorizeMachine=[];
            }


            $scope.applicationArray = [];
            var param = 'applicationName=' + $scope.applicationName+'&machineId=' + $scope.machineId+ '&projectId=' + $scope.projectId+ "&pageNum=" + $scope.pageNum + "&pageSize=" + $scope.pageSize;

            $http({
                url: serverHost + 'monitorApplication/searchApplicationByApplicationName' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {

                if (rsp.data.resultCode == 0) {
                    console.log("invoke searchFunction ok:", rsp.data.data);

                    //设置列表内容
                    $scope.applicationArray = rsp.data.data.list;
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

            //应用名称
            var applicationName=$scope.formData.applicationName;
            if(applicationName==null||applicationName.trim()==''){
                alert('请填写应用名称!');
                return ;
            }

            var saveOrUpdateContextPath='';
            if($scope.data.current==2){
                saveOrUpdateContextPath='monitorApplication/editApplication';
            }else if($scope.data.current==3){
                saveOrUpdateContextPath='monitorApplication/addApplication';
            }
            console.log('$scope.formData.machineId '+$scope.formData.machineId);
            $http({
                url: serverHost + saveOrUpdateContextPath ,
                withCredentials: true,
                method: 'POST',
                data:{
                    id:$scope.formData.id,
                    applicationName:applicationName,
                    projectId:$scope.formData.projectId,
                    machineId:$scope.formData.machineId,
                    applicationDetailParam:$scope.formData.applicationDetailParam,
                    applicationType:$scope.formData.applicationType,
                    applicationDesc:$scope.formData.applicationDesc
                }
            }).then((rsp) => {
                console.log("invoke "+saveOrUpdateContextPath+" ok:", rsp.data.resultCode, rsp.data.resultMsg);

                if (rsp.data.resultCode == 0) {
                    alert('保存成功！');
                    //重新拉取监控应用
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

        //搜索所有授权的机器
        $scope.searchAllAuthorizeMachines = function (serverHost,projectId,fun) {

            $http({
                url: serverHost + 'monitorMachine/searchAllAuthorizeMachinesByProjectId?projectId='+projectId,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                if (rsp.data.resultCode == 0) {
                    console.log("invoke searchAllAuthorizeMachines ok:", rsp.data.data);
                    //设置列表内容
                    this.ctrl.allAuthorizeMachine=[];
                    this.ctrl.allAuthorizeMachine = rsp.data.data;
                    // if(this.ctrl.allAuthorizeMachine.length>0){
                    //     $scope.formData.machineId=this.ctrl.allAuthorizeMachine[0].id.toString();
                    //     console.log('$scope.formData.machineId '+$scope.formData.machineId);
                    // }
                    if(fun!=null){
                        fun(rsp.data.data);
                    }

                } else {
                    alert('获取所有授权的机器失败!具体原因：' + rsp.data.resultMsg);
                }
            }, err => {
                console.log("invoke searchAllAuthorizeMachines err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        };


        $scope.selectChangeFormDataProject=function(serverHost){
            $scope.searchAllAuthorizeMachines(serverHost,$scope.formData.projectId,$scope.initAddMonitorApplicationTab);

        }
        //清空新建or编辑Tab页面
        $scope.clearNewOrEditMonitorApplicationTab=function(serverHost,flag){

            /********************************基本属性************************************/
            //应用id
            $scope.formData.id=null;
            //应用名称
            $scope.formData.applicationName="";
            //描述
            $scope.formData.applicationDesc="";
            //应用端口
            $scope.formData.applicationDetailParam="";
            //应用类型
            $scope.formData.applicationType="1";

            //所属项目
            if(this.ctrl.allAuthorizeProject.length>0){
                $scope.formData.projectId=this.ctrl.allAuthorizeProject[0].id.toString();
            }

            //所属机器
            if(flag=='new'){
                $scope.searchAllAuthorizeMachines(serverHost,$scope.formData.projectId,$scope.initAddMonitorApplicationTab);
            }else{
                $scope.searchAllAuthorizeMachines(serverHost,$scope.formData.projectId,null);
            }

        }
        $scope.initAddMonitorApplicationTab=function(allAuthorizeMachine){
            if(allAuthorizeMachine.length>0){
                $scope.formData.machineId=allAuthorizeMachine[0].id.toString();
                //赋值了
            }
        }

        $scope.showAddMonitorApplicationTab=function (serverHost) {
            //清空新建Tab页面
            $scope.clearNewOrEditMonitorApplicationTab(serverHost,'new');
            //跳转到新建页面
            $scope.actions.setCurrent(3);
        }

        $scope.showEditMonitorApplicationTab=function (serverHost,id) {
            //清空编辑Tab页面
            $scope.clearNewOrEditMonitorApplicationTab(serverHost,'edit');
            var param = 'id=' + id;
            $http({
                url: serverHost + 'monitorApplication/getApplicationByApplicationId' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                console.log("invoke getApplicationByApplicationId ok:", rsp.data.resultCode, rsp.data.resultMsg,rsp.data.data);

                if (rsp.data.resultCode == 0) {

                    /********************************基本属性************************************/
                    //应用id
                    $scope.formData.id=rsp.data.data.id;
                    //应用名称
                    $scope.formData.applicationName=rsp.data.data.applicationName;
                    //描述
                    $scope.formData.applicationDesc=rsp.data.data.applicationDesc;
                    //所属机器
                    $scope.formData.machineId=rsp.data.data.machineId.toString();

                    $scope.formData.applicationType=rsp.data.data.applicationType.toString();
                    //应用端口
                    $scope.formData.applicationDetailParam=rsp.data.data.applicationDetailParam;

                    //跳转到编辑页面
                    $scope.actions.setCurrent(2);
                } else {
                    alert('获取应用记录失败！具体原因：' + rsp.data.resultMsg + "。");
                }
            }, err => {
                console.log("invoke getApplicationByApplicationName err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        }

        //删除
        $scope.deleteApplicationFunction = function (serverHost, applicationName,id) {

            if (!confirm("确定要删除" + applicationName + "吗？")) {
                return;
            }

            var param = 'id=' + id;
            $http({
                url: serverHost + 'monitorApplication/deleteApplication' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                console.log("invoke deleteApplication ok:", rsp.data.resultCode, rsp.data.resultMsg);
                if (rsp.data.resultCode == 0) {
                    //重新拉取监控应用
                    $scope.searchFunction(serverHost);
                } else {
                    alert('删除失败！具体原因：' + rsp.data.resultMsg + "。");
                }

            }, err => {
                console.log("invoke deleteApplication err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        };

        //下一页
        $scope.nextPageFunction = function (serverHost) {
            $scope.pageNum += 1;
            //重新拉取监控应用
            $scope.searchFunction(serverHost);
        };

        //上一页
        $scope.lastPageFunction = function (serverHost) {
            $scope.pageNum -= 1;
            //重新拉取监控应用
            $scope.searchFunction(serverHost);
        };
    }
}

export {
    MonitorManageCtrl as PanelCtrl
};


