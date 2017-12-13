///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
//web服务端地址
var serverUrl='http://localhost:8075/';
import {MetricsPanelCtrl} from 'app/plugins/sdk';
import _ from 'lodash';

class MonitorManageCtrl extends MetricsPanelCtrl {
  static templateUrl = 'partials/module.html';

  defaults = {

  };

  /** @ngInject **/
  constructor($scope, $injector, private $http, private uiSegmentSrv) {
    super($scope, $injector);

    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
    this.events.on('panel-initialized', this.onPanelInitalized.bind(this));

    // defaults configs
    _.defaultsDeep(this.panel, this.defaults);

  }

  onPanelInitalized() {

  }
  public  onInitEditMode() {

  }

  monitorManageController($scope, $http) {
      $scope.taskName = "";
      $scope.taskArray =[];


      //搜索功能
      $scope.searchFunction=function(){
          $scope.taskArray =[];
          var param='taskName='+$scope.taskName;
          $http({
              url: serverUrl+'monitortask/searchtaskbytaskname'+"?"+param,
              method: 'GET'
          }).then((rsp) => {
              console.log("invoke searchFunction ok:", rsp.data.data);
              $scope.taskArray=rsp.data.data;
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


