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
    }

    onPanelInitalized() {

    }

    public onInitEditMode() {
        this.addEditorTab('Options', 'public/plugins/monitor-manage-panel/partials/option.html', 1);
    }

    changeServerHost(object) {
        // alert(this.serverHost);
        // console.log(object);
    }


    monitorManageController($scope, $http) {
        //查询参数
        $scope.machineName='';
        //列表内容
        $scope.machineArray = [];
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
            machineName:'',
            machineDesc:''
        };

        //搜索功能
        $scope.searchFunction = function (serverHost) {
            $scope.machineName=document.getElementById('machineName');
            $scope.machineName=($scope.machineName==null? "":$scope.machineName.value);

            $scope.machineArray = [];
            var param = 'machineName=' + $scope.machineName+ "&pageNum=" + $scope.pageNum + "&pageSize=" + $scope.pageSize;
            $http({
                url: serverHost + 'monitorMachine/searchMachineByMachineName' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {

                if (rsp.data.resultCode == 0) {
                    console.log("invoke searchFunction ok:", rsp.data.data);
                    //设置列表内容
                    $scope.machineArray = rsp.data.data.list;
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


            //项目名称
            var machineName=$scope.formData.machineName;
            if(machineName==null||machineName.trim()==''){
                alert('请填写项目名称!');
                return ;
            }

            var saveOrUpdateContextPath='';
            if($scope.data.current==2){
                saveOrUpdateContextPath='monitorMachine/editMachine';
            }else if($scope.data.current==3){
                saveOrUpdateContextPath='monitorMachine/addMachine';
            }
            console.log($scope.formData.id+" "+machineName+" "+$scope.formData.machineDesc);
            $http({
                url: serverHost + saveOrUpdateContextPath ,
                withCredentials: true,
                method: 'POST',
                data:{
                    id:$scope.formData.id,
                    machineName:machineName,
                    machineDesc:$scope.formData.machineDesc
                }
            }).then((rsp) => {
                console.log("invoke "+saveOrUpdateContextPath+" ok:", rsp.data.resultCode, rsp.data.resultMsg);

                if (rsp.data.resultCode == 0) {
                    alert('保存成功！');
                    //重新拉取监控项目
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

        //清空新建or编辑Tab页面
        $scope.clearNewOrEditMonitorMachineTab=function(){

            /********************************基本属性************************************/
            //项目id
            $scope.formData.id=null;
            //项目名称
            $scope.formData.machineName="";
            //描述
            $scope.formData.machineDesc="";
        }

        $scope.showAddMonitorMachineTab=function () {
            //清空新建Tab页面
            $scope.clearNewOrEditMonitorMachineTab();
            //跳转到新建页面
            $scope.actions.setCurrent(3);
        }

        $scope.showEditMonitorMachineTab=function (serverHost,id) {
            //清空编辑Tab页面
            $scope.clearNewOrEditMonitorMachineTab();
            var param = 'id=' + id;
            $http({
                url: serverHost + 'monitorMachine/getMachineByMachineId' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                console.log("invoke getMachineByMachineId ok:", rsp.data.resultCode, rsp.data.resultMsg,rsp.data.data);

                if (rsp.data.resultCode == 0) {

                    /********************************基本属性************************************/
                    //项目id
                    $scope.formData.id=rsp.data.data.id;
                    //项目名称
                    $scope.formData.machineName=rsp.data.data.machineName;
                    //数据源Ip
                    $scope.formData.machineDesc=rsp.data.data.machineDesc;
                    //跳转到编辑页面
                    $scope.actions.setCurrent(2);
                } else {
                    alert('获取项目记录失败！具体原因：' + rsp.data.resultMsg + "。");
                }
            }, err => {
                console.log("invoke getMachineByMachineName err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        }

        //删除
        $scope.deleteMachineFunction = function (serverHost, machineName,id) {

            if (!confirm("确定要删除" + machineName + "吗？")) {
                return;
            }

            var param = 'id=' + id;
            $http({
                url: serverHost + 'monitorMachine/deleteMachine' + "?" + param,
                withCredentials: true,
                method: 'GET'
            }).then((rsp) => {
                console.log("invoke deleteMachine ok:", rsp.data.resultCode, rsp.data.resultMsg);
                if (rsp.data.resultCode == 0) {
                    //重新拉取监控项目
                    $scope.searchFunction(serverHost);
                } else {
                    alert('删除失败！具体原因：' + rsp.data.resultMsg + "。");
                }

            }, err => {
                console.log("invoke deleteMachine err:", err);
                alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
            });
        };

        //下一页
        $scope.nextPageFunction = function (serverHost) {
            $scope.pageNum += 1;
            //重新拉取监控项目
            $scope.searchFunction(serverHost);
        };

        //上一页
        $scope.lastPageFunction = function (serverHost) {
            $scope.pageNum -= 1;
            //重新拉取监控项目
            $scope.searchFunction(serverHost);
        };
    }
}

export {
    MonitorManageCtrl as PanelCtrl
};


