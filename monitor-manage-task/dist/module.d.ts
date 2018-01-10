/// <reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import { MetricsPanelCtrl } from 'app/plugins/sdk';
declare class MonitorManageCtrl extends MetricsPanelCtrl {
    private $http;
    private uiSegmentSrv;
    static templateUrl: string;
    defaults: {};
    /** @ngInject **/
    constructor($scope: any, $injector: any, $http: any, uiSegmentSrv: any);
    onPanelInitalized(): void;
    allAuthorizeProject: any[];
    allAuthorizeMachine: any[];
    allAuthorizeMachineChecked: any[];
    onInitEditMode(): void;
    searchAllAuthorizeProjects: (serverHost: any) => void;
    changeServerHost(object: any): void;
    monitorManageController($scope: any, $http: any): void;
}
export { MonitorManageCtrl as PanelCtrl };
