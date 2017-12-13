///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />

import {MetricsPanelCtrl} from 'app/plugins/sdk';
import _ from 'lodash';
import {MonitorTaskService} from './ts/service/MonitorTaskService';
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
      $scope.taskName = "任务test3";
      //搜索功能
      $scope.searchFunction=function(){
          alert('controller中search');
          MonitorTaskService.searchTask($http,$scope.taskName);
      };
      //新增功能
      $scope.addFunction=function(){
          alert('controller中add');
          MonitorTaskService.addTask($http);
      };
      //启动/暂停
      $scope.startOrStopTask=function(id){
          alert('controller中add');
          MonitorTaskService.startOrStopTask($http,id);
      };
      //编辑
      $scope.editTask=function(id){
          alert('controller中add');
          MonitorTaskService.editTask($http,id);
      };
      //删除
      $scope.deleteTask=function(id){
          alert('controller中add');
          MonitorTaskService.deleteTask($http,id);
      };
  }
}

    export {
      MonitorManageCtrl as PanelCtrl
    };


