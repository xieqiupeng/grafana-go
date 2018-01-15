///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
System.register(['app/plugins/sdk', 'lodash', './css/module.css!'], function(exports_1) {
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var sdk_1, lodash_1;
    var panelDefaults, MonitorManageCtrl;
    return {
        setters:[
            function (sdk_1_1) {
                sdk_1 = sdk_1_1;
            },
            function (lodash_1_1) {
                lodash_1 = lodash_1_1;
            },
            function (_1) {}],
        execute: function() {
            // import './css/jqui.css!';
            panelDefaults = {
                serverHost: 'http://127.0.0.1:8080/'
            };
            MonitorManageCtrl = (function (_super) {
                __extends(MonitorManageCtrl, _super);
                /** @ngInject **/
                function MonitorManageCtrl($scope, $injector, $http, uiSegmentSrv) {
                    _super.call(this, $scope, $injector);
                    this.$http = $http;
                    this.uiSegmentSrv = uiSegmentSrv;
                    this.defaults = {};
                    this.allAuthorizeProject = [];
                    //搜索所有授权的项目
                    this.searchAllAuthorizeProjects = function (serverHost) {
                        var _this = this;
                        this.allAuthorizeProject = [];
                        this.$http({
                            url: serverHost + 'monitorProject/searchAllAuthorizeProjects',
                            withCredentials: true,
                            method: 'GET'
                        }).then(function (rsp) {
                            if (rsp.data.resultCode == 0) {
                                console.log("invoke searchAllAuthorizeProjects ok:", rsp.data.data);
                                //设置列表内容
                                _this.allAuthorizeProject = rsp.data.data;
                            }
                            else {
                                alert('获取所有授权的项目失败!具体原因：' + rsp.data.resultMsg);
                            }
                        }, function (err) {
                            console.log("invoke searchAllAuthorizeProjects err:", err);
                            alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
                        });
                    };
                    // defaults configs
                    lodash_1.default.defaultsDeep(this.panel, panelDefaults);
                    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
                    // this.events.on('panel-initialized', this.onPanelInitalized.bind(this));
                    this.events.on('panel-initialized', this.render.bind(this));
                    this.panel.title = '机器管理';
                    this.searchAllAuthorizeProjects(panelDefaults.serverHost);
                }
                MonitorManageCtrl.prototype.onPanelInitalized = function () {
                };
                MonitorManageCtrl.prototype.onInitEditMode = function () {
                    this.addEditorTab('Options', 'public/plugins/monitor-manage-machine-panel/partials/option.html', 1);
                };
                MonitorManageCtrl.prototype.changeServerHost = function (object) {
                    // alert(this.serverHost);
                    // console.log(object);
                };
                MonitorManageCtrl.prototype.monitorManageController = function ($scope, $http) {
                    //查询参数
                    $scope.machineName = '';
                    //查询参数
                    $scope.projectId = '';
                    //列表内容
                    $scope.machineArray = [];
                    //分页参数
                    $scope.total = 0; //总条数
                    $scope.pages = 0; //总页面
                    $scope.pageNum = 0; //当前页面
                    $scope.pageSize = 5; //页面大小
                    $scope.hasPreviousPage = false; //有前一页
                    $scope.hasNextPage = false; //有后一页
                    $scope.data = {
                        current: 1 // 1代表查询，2代表编辑,3代表新建
                    };
                    $scope.actions = {
                        setCurrent: function (param) {
                            $scope.data.current = param;
                        }
                    };
                    $scope.formData = {
                        /************基本属性***********/
                        id: null,
                        machineName: '',
                        operateSystemType: "0",
                        machineIp: '',
                        projectId: '',
                        machineDesc: ''
                    };
                    $scope.selectChangeProject = function (serverHost) {
                        $scope.searchFunction(serverHost);
                    };
                    //搜索功能
                    $scope.searchFunction = function (serverHost) {
                        var _this = this;
                        // if($scope.machineName==''){
                        $scope.machineName = document.getElementById('machineName');
                        $scope.machineName = ($scope.machineName == null ? "" : $scope.machineName.value);
                        // }
                        console.log("before projectId:" + $scope.projectId);
                        // if($scope.projectId==''){
                        $scope.projectId = document.getElementById('projectId');
                        $scope.projectId = ($scope.projectId == null ? "" : $scope.projectId.value);
                        // }
                        console.log("after projectId:" + $scope.projectId);
                        $scope.machineArray = [];
                        var param = 'machineName=' + $scope.machineName + '&projectId=' + $scope.projectId + "&pageNum=" + $scope.pageNum + "&pageSize=" + $scope.pageSize;
                        $http({
                            url: serverHost + 'monitorMachine/searchMachineByMachineName' + "?" + param,
                            withCredentials: true,
                            method: 'GET'
                        }).then(function (rsp) {
                            if (rsp.data.resultCode == 0) {
                                console.log("invoke searchFunction ok:", rsp.data.data);
                                for (var i = 0; i < rsp.data.data.list.length; i++) {
                                    for (var j = 0; j < _this.ctrl.allAuthorizeProject.length; j++) {
                                        if (rsp.data.data.list[i].projectId == _this.ctrl.allAuthorizeProject[j].id) {
                                            rsp.data.data.list[i].projectId = _this.ctrl.allAuthorizeProject[j].projectName;
                                            break;
                                        }
                                    }
                                }
                                //设置列表内容
                                $scope.machineArray = rsp.data.data.list;
                                //设置分页内容
                                $scope.pageNum = rsp.data.data.pageNum; //当前页面
                                $scope.total = rsp.data.data.total; //总条数
                                $scope.pages = rsp.data.data.pages; //总页面
                                $scope.hasPreviousPage = rsp.data.data.hasPreviousPage; //有前一页
                                $scope.hasNextPage = rsp.data.data.hasNextPage; //有后一页
                            }
                            else {
                                alert('解析失败!具体原因：' + rsp.data.resultMsg);
                            }
                        }, function (err) {
                            console.log("invoke searchFunction err:", err);
                            alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
                        });
                    };
                    //切换tab时,提示确定退出
                    $scope.confirmChangeTab = function (serverHost) {
                        if ($scope.data.current != 1) {
                            var tabName = ($scope.data.current == 2 ? '编辑' : '新建');
                            var result = confirm('确定退出' + tabName + '页面？');
                            if (result) {
                                $scope.actions.setCurrent(1);
                            }
                        }
                    };
                    //保存或者更新
                    $scope.saveOrUpdate = function (serverHost) {
                        //机器名称
                        var machineName = $scope.formData.machineName;
                        if (machineName == null || machineName.trim() == '') {
                            alert('请填写机器名称!');
                            return;
                        }
                        var saveOrUpdateContextPath = '';
                        if ($scope.data.current == 2) {
                            saveOrUpdateContextPath = 'monitorMachine/editMachine';
                        }
                        else if ($scope.data.current == 3) {
                            saveOrUpdateContextPath = 'monitorMachine/addMachine';
                        }
                        $http({
                            url: serverHost + saveOrUpdateContextPath,
                            withCredentials: true,
                            method: 'POST',
                            data: {
                                id: $scope.formData.id,
                                machineName: machineName,
                                projectId: $scope.formData.projectId,
                                machineIp: $scope.formData.machineIp,
                                operateSystemType: $scope.formData.operateSystemType,
                                machineDesc: $scope.formData.machineDesc
                            }
                        }).then(function (rsp) {
                            console.log("invoke " + saveOrUpdateContextPath + " ok:", rsp.data.resultCode, rsp.data.resultMsg);
                            if (rsp.data.resultCode == 0) {
                                alert('保存成功！');
                                //重新拉取监控机器
                                $scope.searchFunction(serverHost);
                                //跳转到查询tab
                                $scope.actions.setCurrent(1);
                            }
                            else {
                                alert('保存失败！具体原因：' + rsp.data.resultMsg + "。");
                            }
                        }, function (err) {
                            console.log("invoke " + saveOrUpdateContextPath + " err:", err);
                            alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
                        });
                    };
                    //清空新建or编辑Tab页面
                    $scope.clearNewOrEditMonitorMachineTab = function () {
                        /********************************基本属性************************************/
                        //机器id
                        $scope.formData.id = null;
                        //机器名称
                        $scope.formData.machineName = "";
                        //描述
                        $scope.formData.machineDesc = "";
                        //机器Ip
                        $scope.formData.machineIp = "";
                        //操作系统
                        $scope.formData.operateSystemType = "0";
                        //所属项目
                        $scope.formData.projectId = this.ctrl.allAuthorizeProject[0].id.toString();
                    };
                    $scope.showAddMonitorMachineTab = function () {
                        //清空新建Tab页面
                        $scope.clearNewOrEditMonitorMachineTab();
                        //跳转到新建页面
                        $scope.actions.setCurrent(3);
                    };
                    $scope.showEditMonitorMachineTab = function (serverHost, id) {
                        //清空编辑Tab页面
                        $scope.clearNewOrEditMonitorMachineTab();
                        var param = 'id=' + id;
                        $http({
                            url: serverHost + 'monitorMachine/getMachineByMachineId' + "?" + param,
                            withCredentials: true,
                            method: 'GET'
                        }).then(function (rsp) {
                            console.log("invoke getMachineByMachineId ok:", rsp.data.resultCode, rsp.data.resultMsg, rsp.data.data);
                            if (rsp.data.resultCode == 0) {
                                /********************************基本属性************************************/
                                //机器id
                                $scope.formData.id = rsp.data.data.id;
                                //机器名称
                                $scope.formData.machineName = rsp.data.data.machineName;
                                //机器描述
                                $scope.formData.machineDesc = rsp.data.data.machineDesc;
                                //所属项目
                                $scope.formData.projectId = rsp.data.data.projectId.toString();
                                //机器Ip
                                $scope.formData.machineIp = rsp.data.data.machineIp;
                                //操作系统
                                $scope.formData.operateSystemType = rsp.data.data.operateSystemType.toString();
                                //跳转到编辑页面
                                $scope.actions.setCurrent(2);
                            }
                            else {
                                alert('获取机器记录失败！具体原因：' + rsp.data.resultMsg + "。");
                            }
                        }, function (err) {
                            console.log("invoke getMachineByMachineName err:", err);
                            alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
                        });
                    };
                    //删除
                    $scope.deleteMachineFunction = function (serverHost, machineName, id) {
                        if (!confirm("确定要删除" + machineName + "吗？")) {
                            return;
                        }
                        var param = 'id=' + id;
                        $http({
                            url: serverHost + 'monitorMachine/deleteMachine' + "?" + param,
                            withCredentials: true,
                            method: 'GET'
                        }).then(function (rsp) {
                            console.log("invoke deleteMachine ok:", rsp.data.resultCode, rsp.data.resultMsg);
                            if (rsp.data.resultCode == 0) {
                                //重新拉取监控机器
                                $scope.searchFunction(serverHost);
                            }
                            else {
                                alert('删除失败！具体原因：' + rsp.data.resultMsg + "。");
                            }
                        }, function (err) {
                            console.log("invoke deleteMachine err:", err);
                            alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
                        });
                    };
                    //下一页
                    $scope.nextPageFunction = function (serverHost) {
                        $scope.pageNum += 1;
                        //重新拉取监控机器
                        $scope.searchFunction(serverHost);
                    };
                    //上一页
                    $scope.lastPageFunction = function (serverHost) {
                        $scope.pageNum -= 1;
                        //重新拉取监控机器
                        $scope.searchFunction(serverHost);
                    };
                };
                MonitorManageCtrl.templateUrl = 'partials/module.html';
                return MonitorManageCtrl;
            })(sdk_1.MetricsPanelCtrl);
            exports_1("PanelCtrl", MonitorManageCtrl);
        }
    }
});
//# sourceMappingURL=module.js.map