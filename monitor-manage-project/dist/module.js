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
            panelDefaults = {};
            MonitorManageCtrl = (function (_super) {
                __extends(MonitorManageCtrl, _super);
                /** @ngInject **/
                function MonitorManageCtrl($scope, $injector, $http, uiSegmentSrv) {
                    _super.call(this, $scope, $injector);
                    this.$http = $http;
                    this.uiSegmentSrv = uiSegmentSrv;
                    this.defaults = {};
                    // defaults configs
                    lodash_1.default.defaultsDeep(this.panel, panelDefaults);
                    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
                    // this.events.on('panel-initialized', this.onPanelInitalized.bind(this));
                    this.events.on('panel-initialized', this.render.bind(this));
                    this.panel.title = '项目管理';
                }
                MonitorManageCtrl.prototype.onPanelInitalized = function () {
                };
                MonitorManageCtrl.prototype.onInitEditMode = function () {
                    this.addEditorTab('Options', 'public/plugins/monitor-manage-project-panel/partials/option.html', 1);
                };
                MonitorManageCtrl.prototype.changeServerHost = function (object) {
                    // alert(this.serverHost);
                    // console.log(object);
                };
                MonitorManageCtrl.prototype.monitorManageController = function ($scope, $http) {
                    //查询参数
                    $scope.projectName = '';
                    //列表内容
                    $scope.projectArray = [];
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
                        projectName: '',
                        projectDesc: ''
                    };
                    //搜索功能
                    $scope.searchFunction = function (serverHost) {
                        $scope.projectName = document.getElementById('projectName');
                        $scope.projectName = ($scope.projectName == null ? "" : $scope.projectName.value);
                        $scope.projectArray = [];
                        var param = 'projectName=' + $scope.projectName + "&pageNum=" + $scope.pageNum + "&pageSize=" + $scope.pageSize;
                        $http({
                            url: serverHost + 'monitorProject/searchProjectByProjectName' + "?" + param,
                            withCredentials: true,
                            method: 'GET'
                        }).then(function (rsp) {
                            if (rsp.data.resultCode == 0) {
                                console.log("invoke searchFunction ok:", rsp.data.data);
                                //设置列表内容
                                $scope.projectArray = rsp.data.data.list;
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
                        //项目名称
                        var projectName = $scope.formData.projectName;
                        if (projectName == null || projectName.trim() == '') {
                            alert('请填写项目名称!');
                            return;
                        }
                        var saveOrUpdateContextPath = '';
                        if ($scope.data.current == 2) {
                            saveOrUpdateContextPath = 'monitorProject/editProject';
                        }
                        else if ($scope.data.current == 3) {
                            saveOrUpdateContextPath = 'monitorProject/addProject';
                        }
                        console.log($scope.formData.id + " " + projectName + " " + $scope.formData.projectDesc);
                        $http({
                            url: serverHost + saveOrUpdateContextPath,
                            withCredentials: true,
                            method: 'POST',
                            data: {
                                id: $scope.formData.id,
                                projectName: projectName,
                                projectDesc: $scope.formData.projectDesc
                            }
                        }).then(function (rsp) {
                            console.log("invoke " + saveOrUpdateContextPath + " ok:", rsp.data.resultCode, rsp.data.resultMsg);
                            if (rsp.data.resultCode == 0) {
                                alert('保存成功！');
                                //重新拉取监控项目
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
                    $scope.clearNewOrEditMonitorProjectTab = function () {
                        /********************************基本属性************************************/
                        //项目id
                        $scope.formData.id = null;
                        //项目名称
                        $scope.formData.projectName = "";
                        //描述
                        $scope.formData.projectDesc = "";
                    };
                    $scope.showAddMonitorProjectTab = function () {
                        //清空新建Tab页面
                        $scope.clearNewOrEditMonitorProjectTab();
                        //跳转到新建页面
                        $scope.actions.setCurrent(3);
                    };
                    $scope.showEditMonitorProjectTab = function (serverHost, id) {
                        //清空编辑Tab页面
                        $scope.clearNewOrEditMonitorProjectTab();
                        var param = 'id=' + id;
                        $http({
                            url: serverHost + 'monitorProject/getProjectByProjectId' + "?" + param,
                            withCredentials: true,
                            method: 'GET'
                        }).then(function (rsp) {
                            console.log("invoke getProjectByProjectId ok:", rsp.data.resultCode, rsp.data.resultMsg, rsp.data.data);
                            if (rsp.data.resultCode == 0) {
                                /********************************基本属性************************************/
                                //项目id
                                $scope.formData.id = rsp.data.data.id;
                                //项目名称
                                $scope.formData.projectName = rsp.data.data.projectName;
                                //数据源Ip
                                $scope.formData.projectDesc = rsp.data.data.projectDesc;
                                //跳转到编辑页面
                                $scope.actions.setCurrent(2);
                            }
                            else {
                                alert('获取项目记录失败！具体原因：' + rsp.data.resultMsg + "。");
                            }
                        }, function (err) {
                            console.log("invoke getProjectByProjectName err:", err);
                            alert("连接后台服务异常,请检查options中serverHost地址是否连通！");
                        });
                    };
                    //删除
                    $scope.deleteProjectFunction = function (serverHost, projectName, id) {
                        if (!confirm("确定要删除" + projectName + "吗？")) {
                            return;
                        }
                        var param = 'id=' + id;
                        $http({
                            url: serverHost + 'monitorProject/deleteProject' + "?" + param,
                            withCredentials: true,
                            method: 'GET'
                        }).then(function (rsp) {
                            console.log("invoke deleteProject ok:", rsp.data.resultCode, rsp.data.resultMsg);
                            if (rsp.data.resultCode == 0) {
                                //重新拉取监控项目
                                $scope.searchFunction(serverHost);
                            }
                            else {
                                alert('删除失败！具体原因：' + rsp.data.resultMsg + "。");
                            }
                        }, function (err) {
                            console.log("invoke deleteProject err:", err);
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
                };
                MonitorManageCtrl.templateUrl = 'partials/module.html';
                return MonitorManageCtrl;
            })(sdk_1.MetricsPanelCtrl);
            exports_1("PanelCtrl", MonitorManageCtrl);
        }
    }
});
//# sourceMappingURL=module.js.map