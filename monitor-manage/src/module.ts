///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />

import {MetricsPanelCtrl, PanelCtrl} from 'app/plugins/sdk';
import _ from 'lodash';
import './css/module.css!';
import './css/jqui.css!';
// import './css/bs.css!';
import './js/jq.js';
import './js/jqui.js';
import './js/bs.js';

const panelDefaults = {
    serverHost:'http://127.0.0.1:8080/'
};

class MonitorManageCtrl extends MetricsPanelCtrl {
  static templateUrl = 'partials/module.html';

  defaults = {

  };

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
      $scope.pageSize=5;//页面大小
      $scope.hasPreviousPage=false;//有前一页
      $scope.hasNextPage=false;//有后一页

      //分隔符数组
      $scope.separatorArray=["*","-","|"];

      $scope.test=function(serverHost){
          $scope.searchFunction(serverHost);
      }


      //搜索功能
      $scope.searchFunction=function(serverHost){
          $scope.taskArray =[];

          var param='taskName='+$scope.searchTaskName+"&pageNum="+$scope.pageNum+"&pageSize="+$scope.pageSize;
          $http({
              url: serverHost+'monitorTask/searchTaskByTaskName'+"?"+param,
              withCredentials: true,
              method: 'GET'
          }).then((rsp) => {

              if(rsp.data.resultCode==0){
                  console.log("invoke searchFunction ok:", rsp.data.data);
                  //设置列表内容

                  for(var i=0;i<rsp.data.data.list.length;i++){
                      //处理状态
                      if(0==rsp.data.data.list[i].status){
                          rsp.data.data.list[i].status='启动';
                      }else if(1==rsp.data.data.list[i].status){
                          rsp.data.data.list[i].status='暂停';
                      }
                      //处理tomcat服务器
                      var tomcatServerHostStr="";
                      if(rsp.data.data.list[i].tomcatServerHost!=null&&""!=rsp.data.data.list[i].tomcatServerHost){
                          var tomcatServerHostArray=rsp.data.data.list[i].tomcatServerHost.split(",");

                          for(var j=0;j<tomcatServerHostArray.length;j++){
                              tomcatServerHostStr=tomcatServerHostStr+tomcatServerHostArray[j]+"<br/>";
                          }
                      }
                      rsp.data.data.list[i].tomcatServerHost=tomcatServerHostStr;



                      //处理数据源服务器ip
                      var dataSourceServerIpStr="";
                      if(rsp.data.data.list[i].dataSourceServerIp!=null&&""!=rsp.data.data.list[i].dataSourceServerIp){
                          var dataSourceServerIpArray=rsp.data.data.list[i].dataSourceServerIp.split(",");

                          for(var j=0;j<dataSourceServerIpArray.length;j++){
                              dataSourceServerIpStr=dataSourceServerIpStr+dataSourceServerIpArray[j]+"<br/>";
                          }
                      }
                      rsp.data.data.list[i].dataSourceServerIp=dataSourceServerIpStr;


                  }
                  $scope.taskArray=rsp.data.data.list;
                  //设置分页内容
                  $scope.pageNum=rsp.data.data.pageNum; //当前页面
                  $scope.total=rsp.data.data.total;//总条数
                  $scope.pages=rsp.data.data.pages; //总页面
                  $scope.hasPreviousPage=rsp.data.data.hasPreviousPage;//有前一页
                  $scope.hasNextPage=rsp.data.data.hasNextPage;//有后一页


              }else{
                  alert('解析失败!具体原因：'+rsp.data.resultMsg);
              }




          }, err => {
              console.log("invoke searchFunction err:", err);
              alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
          });
      };

      //启动/暂停
      $scope.startOrPauseTaskFuction=function(serverHost,taskName,status){
          var param='taskName='+taskName+'&status='+status;
          $http({
              url: serverHost+'monitorTask/startOrPauseTask'+"?"+param,
              withCredentials: true,
              method: 'GET'
          }).then((rsp) => {
              console.log("invoke startOrPauseTask ok:", rsp.data.resultCode,rsp.data.resultMsg);

              if(rsp.data.resultCode==0){
                  //重新拉取监控任务
                  $scope.searchFunction(serverHost);
              }else{
                  alert('启动/暂停失败！具体原因：'+rsp.data.resultMsg+"。");
              }

          }, err => {
              console.log("invoke startOrPauseTask err:", err);
              alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
          });
      };

      //删除
      $scope.deleteTaskFunction=function(serverHost,taskName){

          if(!confirm("确定要删除"+taskName+"吗？"))
          {
              return;
          }

          var param='taskName='+taskName;
          $http({
              url: serverHost+'monitorTask/deleteTask'+"?"+param,
              withCredentials: true,
              method: 'GET'
          }).then((rsp) => {
              console.log("invoke deleteTask ok:", rsp.data.resultCode,rsp.data.resultMsg);
              if(rsp.data.resultCode==0){
                  //重新拉取监控任务
                  $scope.searchFunction(serverHost);
              }else{
                  alert('删除失败！具体原因：'+rsp.data.resultMsg+"。");
              }

          }, err => {
              console.log("invoke deleteTask err:", err);
              alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
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
  }
}

    export {
      MonitorManageCtrl as PanelCtrl
    };


