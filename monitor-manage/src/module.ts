///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
//web服务端地址
var serverUrl='http://localhost:8080/';
import {MetricsPanelCtrl, PanelCtrl} from 'app/plugins/sdk';
import _ from 'lodash';
import './css/module.css!';
import './css/jqui.css!';
// import './css/bs.css!';
import './js/jq.js';
import './js/jqui.js';
import './js/bs.js';

const panelDefaults = {
};

class MonitorManageCtrl extends MetricsPanelCtrl {
  static templateUrl = 'partials/module.html';
  serverHost="http://127.0.0.1:8080/";
  defaults = {

  };

  /** @ngInject **/
  constructor($scope, $injector, private $http, private uiSegmentSrv) {
      // super($scope, $injector);
    super($scope, $injector);
      // defaults configs
      _.defaultsDeep(this.panel, panelDefaults);
    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
    // this.events.on('panel-initialized', this.onPanelInitalized.bind(this));
    this.events.on('panel-initialized', this.render.bind(this));


  }

  onPanelInitalized() {

  }
  public  onInitEditMode() {
      this.addEditorTab('Options', 'public/plugins/monitor-manage-panel/partials/option.html', 1);
  }

  changeServerHost(object){
      // alert(this.serverHost);
      // console.log(object);
  }


  monitorManageController($scope, $http) {
      //查询参数
      $scope.searchTaskName = "";
      //列表内容
      $scope.taskArray =[];
      //分页参数
      $scope.total=0; //总条数
      $scope.pages=0; //总页面
      $scope.pageNum=0; //当前页面
      $scope.pageSize=10;//页面大小
      $scope.hasPreviousPage=false;//有前一页
      $scope.hasNextPage=false;//有后一页

      //搜索功能
      $scope.searchFunction=function(serverHost){
          $scope.taskArray =[];

          var param='taskName='+$scope.searchTaskName+"&pageNum="+$scope.pageNum+"&pageSize="+$scope.pageSize;
          $http({
              url: serverHost+'monitortask/searchtaskbytaskname'+"?"+param,
              method: 'GET'
          }).then((rsp) => {
              console.log("invoke searchFunction ok:", rsp.data.data);
              //设置列表内容
              $scope.taskArray=rsp.data.data.list;
              //设置分页内容
              $scope.pageNum=rsp.data.data.pageNum; //当前页面
              $scope.total=rsp.data.data.total;//总条数
              $scope.pages=rsp.data.data.pages; //总页面
              $scope.hasPreviousPage=rsp.data.data.hasPreviousPage;//有前一页
              $scope.hasNextPage=rsp.data.data.hasNextPage;//有后一页
          }, err => {
              console.log("invoke searchFunction err:", err);
          });
      };

      //启动/暂停
      $scope.startOrPauseTaskFuction=function(id){
          var param='taskId='+id;
          $http({
              url: serverUrl+'monitortask/startorpausetask'+"?"+param,
              method: 'GET'
          }).then((rsp) => {
              console.log("invoke startorpausetask ok:", rsp.data.resultCode,rsp.data.resultMsg);
              //重新拉取监控任务
              $scope.searchFunction();
          }, err => {
              console.log("invoke startorpausetask err:", err);
          });
      };

      //删除
      $scope.deleteTaskFunction=function(id){
          var param='taskId='+id;
          $http({
              url: serverUrl+'monitortask/deletetask'+"?"+param,
              method: 'GET'
          }).then((rsp) => {
              console.log("invoke deletetask ok:", rsp.data.resultCode,rsp.data.resultMsg);
              //重新拉取监控任务
              $scope.searchFunction();
          }, err => {
              console.log("invoke startorpausetask err:", err);
          });
      };

      //下一页
      $scope.nextPageFunction=function(serverHost){
          $scope.pageNum+=1;
          //重新拉取监控任务
          $scope.searchFunction(serverHost);
      };

      //上一页
      $scope.lastPageFunction=function(serverHost){
          $scope.pageNum-=1;
          //重新拉取监控任务
          $scope.searchFunction(serverHost);
      };

      // //上一页
      // $scope.selectChangePageSize=function(){
      //     //重新拉取监控任务
      //     $scope.searchFunction();
      // };

      // //新增功能
      // $scope.addFunction=function(){
      //     alert('controller中add');
      //     MonitorTaskService.addTask($http);
      // };

      // //编辑
      // $scope.editTask=function(id){
      //     alert('controller中add');
      //     MonitorTaskService.editTask($http,id);
      // };

  }
}

    export {
      MonitorManageCtrl as PanelCtrl
    };


