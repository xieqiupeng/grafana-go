///<reference path="../../../headers/common.d.ts" />

import _ from 'lodash';
import {PanelCtrl} from 'app/plugins/sdk';
import {impressions} from 'app/features/dashboard/impression_store';

class DashListCtrl extends PanelCtrl {
  static templateUrl = 'module.html';

  groups: any[];
  modes: any[];

  panelDefaults = {
    query: '',
    limit: 10,
    tags: [],
    recent: false,
    search: false,
    starred: true,
    headings: true,
  };

  /** @ngInject */
  constructor($scope, $injector, private backendSrv, private dashboardSrv) {
    super($scope, $injector);
    _.defaults(this.panel, this.panelDefaults);

    if (this.panel.tag) {
      this.panel.tags = [this.panel.tag];
      delete this.panel.tag;
    }

    this.events.on('refresh', this.onRefresh.bind(this));
    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));

    this.groups = [
      {list: [], show: false, header: "星标的仪表板",},
      {list: [], show: false, header: "最近查看的仪表板"},
      {list: [], show: false, header: "搜索"},
    ];

    // update capability
    if (this.panel.mode) {
      if (this.panel.mode === 'starred') {
        this.panel.starred = true;
        this.panel.headings = false;
      }
      if (this.panel.mode === 'recently viewed') {
        this.panel.recent = true;
        this.panel.starred = false;
        this.panel.headings = false;
      }
      if (this.panel.mode === 'search') {
        this.panel.search = true;
        this.panel.starred = false;
        this.panel.headings = false;
      }
      delete this.panel.mode;
    }
  }

  onInitEditMode() {
    this.editorTabIndex = 1;
    this.modes = ['starred', 'search', 'recently viewed'];
    this.addEditorTab('Options', 'public/app/plugins/panel/dashlist/editor.html');
  }

  onRefresh() {
    var promises = [];

    promises.push(this.getRecentDashboards());
    promises.push(this.getStarred());
    promises.push(this.getSearch());

    return Promise.all(promises)
      .then(this.renderingCompleted.bind(this));
  }

  getSearch() {
    this.groups[2].show = this.panel.search;
    if (!this.panel.search) {
      return Promise.resolve();
    }

    var params = {
      limit: this.panel.limit,
      query: this.panel.query,
      tag: this.panel.tags,
    };

    return this.backendSrv.search(params).then(result => {
      this.groups[2].list = result;
    });
  }

  getStarred() {
    this.groups[0].show = this.panel.starred;
    if (!this.panel.starred) {
      return Promise.resolve();
    }

    var params = {limit: this.panel.limit, starred: "true"};
    return this.backendSrv.search(params).then(result => {
      this.groups[0].list = result;
    });
  }

  starDashboard(dash, evt) {
    this.dashboardSrv.starDashboard(dash.id, dash.isStarred).then(newState => {
      dash.isStarred = newState;
    });

    if (evt) {
      evt.stopPropagation();
      evt.preventDefault();
    }
  }

  getRecentDashboards() {
    this.groups[1].show = this.panel.recent;
    if (!this.panel.recent) {
      return Promise.resolve();
    }

    var dashIds = _.take(impressions.getDashboardOpened(), this.panel.limit);
    return this.backendSrv.search({dashboardIds: dashIds, limit: this.panel.limit}).then(result => {
      this.groups[1].list = dashIds.map(orderId => {
        return _.find(result, dashboard => {
          return dashboard.id === orderId;
        });
      }).filter(el => {
        return el !== undefined;
      });
    });
  }
}

export {DashListCtrl, DashListCtrl as PanelCtrl};
