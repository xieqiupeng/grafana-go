///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />

import config from 'app/core/config';
import appEvents from 'app/core/app_events';

import {MetricsPanelCtrl} from  'app/plugins/sdk';

import _ from 'lodash';
import moment from 'moment';


class InfluxAdminCtrl extends MetricsPanelCtrl {
  static templateUrl = 'partials/module.html';

  writing: boolean;
  history: Array<any>;
  dbSeg: any;
  ds: any;

  // The running Queries
  queryInfo: any;
  queryRefresh: any; // $timeout promice

  // Helpers for the html
  clickableQuery: boolean;
  rsp: any; // the raw response from InfluxDB
  rspInfo: string;

  // This is set in the form
  writeDataText: string;
  q: string;

  defaults = {
    mode: 'current', // 'write', 'query'
    query: 'SHOW DIAGNOSTICS',
    database: null,
    time: 'YYYY-MM-DDTHH:mm:ssZ',
    refresh: false,
    refreshInterval: 1200
  };

  /** @ngInject **/
  constructor($scope, $injector, private $http, private uiSegmentSrv) {
    super($scope, $injector);

    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
    this.events.on('panel-initialized', this.onPanelInitalized.bind(this));

    this.writing = false;
    this.history = [  ];


    // defaults configs
    _.defaultsDeep(this.panel, this.defaults);

  }

  onPanelInitalized() {

  }

   postData(){
    alert("postData");
    this.$http({
        url:'http://localhost:8077/monitorTask/searchTask',
        method: 'POST',
        data: '{"data"," post"}'
        //,
        // headers: {
        //     "Content-Type": "plain/text"
        // }
    }).then((rsp) => {
        this.writing = false;
        console.log( "Wrote OK", rsp );
    }, err => {
        this.writing = false;
        console.log( "Wite ERROR", err );
        this.error = err.data.error + " ["+err.status+"]";
        this.inspector = {error: err};
    });
  }

    getData(){
        alert("getData");
        this.$http({
            url:'http://localhost:8077/monitorTask/searchTask?data="wawa"',
            method: 'GET',
            data: '{"data"," get"}'
            //,
            // headers: {
            //     "Content-Type": "plain/text"
            // }
        }).then((rsp) => {
            this.writing = false;
            console.log( "Wrote OK", rsp );
        }, err => {
            this.writing = false;
            console.log( "Wite ERROR", err );
            this.error = err.data.error + " ["+err.status+"]";
            this.inspector = {error: err};
        });
    }



  isShowQueryWindow() {
    return this.panel.mode == 'query';
  }

  isShowCurrentQueries() {
    return this.panel.mode == 'current';
  }


    public  onInitEditMode() {
        this.editorTabIndex = 2;
  }




  getQueryHistory() {
    return this.history;
  }



  setQuery(txt) {
    this.panel.query = txt;
    this.onQueryChanged();
  }

  isClickableQuery() {
    let q = this.q;
    if( q && q.startsWith('SHOW ')) {
      if( "SHOW DATABASES" == q && this.panel.queryDB ) {
        return true;
      }
      if( q.startsWith( 'SHOW MEASUREMENTS')) {
        return true;
      }
      if( q.startsWith( 'SHOW FIELD KEYS')) {
        return true;
      }
    }
    return false;
  }

  onClickedResult(res) {
    console.log( "CLICKED", this.panel.query, res );

    return;
  }

  isPostQuery() {
    var q = this.panel.query;
    return !(
      q.startsWith( "SELECT " ) ||
      q.startsWith( "SHOW " ));
  }

  onQueryChanged() {
    console.log("onQueryChanged()", this.panel.query );
    this.rsp = null;
    if(!this.isPostQuery()) {
      this.doSubmit();
    }
    else {
      console.log("POST query won't submit automatically");
    }
  }

  doSubmit() {

  }

}

export {
  InfluxAdminCtrl as PanelCtrl
};


