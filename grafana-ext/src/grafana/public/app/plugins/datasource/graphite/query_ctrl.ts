import './add_graphite_func';
import './func_editor';

import _ from 'lodash';
import gfunc from './gfunc';
import GraphiteQuery from './graphite_query';
import {QueryCtrl} from 'app/plugins/sdk';
import appEvents from 'app/core/app_events';

const GRAPHITE_TAG_OPERATORS = ['=', '!=', '=~', '!=~'];
const TAG_PREFIX = 'tag: ';

export class GraphiteQueryCtrl extends QueryCtrl {
  static templateUrl = 'partials/query.editor.html';

  queryModel: GraphiteQuery;
  segments: any[];
  addTagSegments: any[];
  removeTagValue: string;
  supportsTags: boolean;

  /** @ngInject **/
  constructor($scope, $injector, private uiSegmentSrv, private templateSrv) {
    super($scope, $injector);
    this.supportsTags = this.datasource.supportsTags;

    if (this.target) {
      this.target.target = this.target.target || '';
      this.queryModel = new GraphiteQuery(this.target, templateSrv);
      this.buildSegments();
    }

    this.removeTagValue = '-- remove tag --';
  }

  parseTarget() {
    this.queryModel.parseTarget();
    this.buildSegments();
  }

  toggleEditorMode() {
    this.target.textEditor = !this.target.textEditor;
    this.parseTarget();
  }

  buildSegments() {
    this.segments = _.map(this.queryModel.segments, segment => {
      return this.uiSegmentSrv.newSegment(segment);
    });

    let checkOtherSegmentsIndex = this.queryModel.checkOtherSegmentsIndex || 0;
    this.checkOtherSegments(checkOtherSegmentsIndex);

    if (this.queryModel.seriesByTagUsed) {
      this.fixTagSegments();
    }
  }

  addSelectMetricSegment() {
    this.queryModel.addSelectMetricSegment();
    this.segments.push(this.uiSegmentSrv.newSelectMetric());
  }

  checkOtherSegments(fromIndex) {
    if (fromIndex === 0) {
      this.addSelectMetricSegment();
      return;
    }

    var path = this.queryModel.getSegmentPathUpTo(fromIndex + 1);
    if (path === "") {
      return Promise.resolve();
    }

    return this.datasource.metricFindQuery(path).then(segments => {
      if (segments.length === 0) {
        if (path !== '') {
          this.queryModel.segments = this.queryModel.segments.splice(0, fromIndex);
          this.segments = this.segments.splice(0, fromIndex);
          this.addSelectMetricSegment();
        }
      } else if (segments[0].expandable) {
        if (this.segments.length === fromIndex) {
          this.addSelectMetricSegment();
        } else {
          return this.checkOtherSegments(fromIndex + 1);
        }
      }
    }).catch(err => {
      appEvents.emit('alert-error', ['Error', err]);
    });
  }

  setSegmentFocus(segmentIndex) {
    _.each(this.segments, (segment, index) => {
      segment.focus = segmentIndex === index;
    });
  }

  getAltSegments(index) {
    var query = index === 0 ? '*' : this.queryModel.getSegmentPathUpTo(index) + '.*';
    var options = {range: this.panelCtrl.range, requestId: "get-alt-segments"};

    return this.datasource.metricFindQuery(query, options).then(segments => {
      var altSegments = _.map(segments, segment => {
        return this.uiSegmentSrv.newSegment({value: segment.text, expandable: segment.expandable});
      });

      if (altSegments.length === 0) { return altSegments; }

      // add template variables
      _.each(this.templateSrv.variables, variable => {
        altSegments.unshift(this.uiSegmentSrv.newSegment({
          type: 'template',
          value: '$' + variable.name,
          expandable: true,
        }));
      });

      // add wildcard option
      altSegments.unshift(this.uiSegmentSrv.newSegment('*'));

      if (this.supportsTags && index === 0) {
        this.removeTaggedEntry(altSegments);
        return this.addAltTagSegments(index, altSegments);
      } else {
        return altSegments;
      }
    }).catch(err => {
      return [];
    });
  }

  addAltTagSegments(index, altSegments) {
    return this.getTagsAsSegments().then((tagSegments) => {
      tagSegments = _.map(tagSegments, (segment) => {
        segment.value = TAG_PREFIX + segment.value;
        return segment;
      });
      return altSegments.concat(...tagSegments);
    });
  }

  removeTaggedEntry(altSegments) {
    altSegments = _.remove(altSegments, (s) => s.value === '_tagged');
  }

  segmentValueChanged(segment, segmentIndex) {
    this.error = null;
    this.queryModel.updateSegmentValue(segment, segmentIndex);

    if (this.queryModel.functions.length > 0 && this.queryModel.functions[0].def.fake) {
      this.queryModel.functions = [];
    }

    if (segment.type === 'tag') {
      let tag = removeTagPrefix(segment.value);
      this.addSeriesByTagFunc(tag);
      return;
    }

    if (segment.expandable) {
      return this.checkOtherSegments(segmentIndex + 1).then(() => {
        this.setSegmentFocus(segmentIndex + 1);
        this.targetChanged();
      });
    } else {
      this.spliceSegments(segmentIndex + 1);
    }

    this.setSegmentFocus(segmentIndex + 1);
    this.targetChanged();
  }

  spliceSegments(index) {
    this.segments = this.segments.splice(0, index);
    this.queryModel.segments = this.queryModel.segments.splice(0, index);
  }

  emptySegments() {
    this.queryModel.segments = [];
    this.segments = [];
  }

  targetTextChanged() {
    this.updateModelTarget();
    this.refresh();
  }

  updateModelTarget() {
    this.queryModel.updateModelTarget(this.panelCtrl.panel.targets);
  }

  targetChanged() {
    if (this.queryModel.error) {
      return;
    }

    var oldTarget = this.queryModel.target.target;
    this.updateModelTarget();

    if (this.queryModel.target !== oldTarget) {
      var lastSegment = this.segments.length > 0 ? this.segments[this.segments.length - 1] : {};
      if (lastSegment.value !== 'select metric') {
        this.panelCtrl.refresh();
      }
    }
  }

  addFunction(funcDef) {
    var newFunc = gfunc.createFuncInstance(funcDef, { withDefaultParams: true });
    newFunc.added = true;
    this.queryModel.addFunction(newFunc);
    this.smartlyHandleNewAliasByNode(newFunc);

    if (this.segments.length === 1 && this.segments[0].fake) {
      this.emptySegments();
    }

    if (!newFunc.params.length && newFunc.added) {
      this.targetChanged();
    }

    if (newFunc.def.name === 'seriesByTag') {
      this.parseTarget();
    }
  }

  removeFunction(func) {
    this.queryModel.removeFunction(func);
    this.targetChanged();
  }

  addSeriesByTagFunc(tag) {
    let funcDef = gfunc.getFuncDef('seriesByTag');
    let newFunc = gfunc.createFuncInstance(funcDef, { withDefaultParams: false });
    let tagParam = `${tag}=select tag value`;
    newFunc.params = [tagParam];
    this.queryModel.addFunction(newFunc);
    newFunc.added = true;

    this.emptySegments();
    this.targetChanged();
    this.parseTarget();
  }

  smartlyHandleNewAliasByNode(func) {
    if (func.def.name !== 'aliasByNode') {
      return;
    }

    for (var i = 0; i < this.segments.length; i++) {
      if (this.segments[i].value.indexOf('*') >= 0) {
        func.params[0] = i;
        func.added = false;
        this.targetChanged();
        return;
      }
    }
  }

  getAllTags() {
    return this.datasource.getTags().then((values) => {
      let altTags = _.map(values, 'text');
      altTags.splice(0, 0, this.removeTagValue);
      return mapToDropdownOptions(altTags);
    });
  }

  getTags(index, tagPrefix) {
    let tagExpressions = this.queryModel.renderTagExpressions(index);
    return this.datasource.getTagsAutoComplete(tagExpressions, tagPrefix)
    .then((values) => {
      let altTags = _.map(values, 'text');
      altTags.splice(0, 0, this.removeTagValue);
      return mapToDropdownOptions(altTags);
    });
  }

  getTagsAsSegments() {
    let tagExpressions = this.queryModel.renderTagExpressions();
    return this.datasource.getTagsAutoComplete(tagExpressions)
    .then((values) => {
      return _.map(values, (val) => {
        return this.uiSegmentSrv.newSegment({value: val.text, type: 'tag', expandable: false});
      });
    });
  }

  getTagOperators() {
    return mapToDropdownOptions(GRAPHITE_TAG_OPERATORS);
  }

  getAllTagValues(tag) {
    let tagKey = tag.key;
    return this.datasource.getTagValues(tagKey).then((values) => {
      let altValues = _.map(values, 'text');
      return mapToDropdownOptions(altValues);
    });
  }

  getTagValues(tag, index, valuePrefix) {
    let tagExpressions = this.queryModel.renderTagExpressions(index);
    let tagKey = tag.key;
    return this.datasource.getTagValuesAutoComplete(tagExpressions, tagKey, valuePrefix).then((values) => {
      let altValues = _.map(values, 'text');
      return mapToDropdownOptions(altValues);
    });
  }

  tagChanged(tag, tagIndex) {
    this.queryModel.updateTag(tag, tagIndex);
    this.targetChanged();
  }

  addNewTag(segment) {
    let newTagKey = segment.value;
    let newTag = {key: newTagKey, operator: '=', value: 'select tag value'};
    this.queryModel.addTag(newTag);
    this.targetChanged();
    this.fixTagSegments();
  }

  removeTag(index) {
    this.queryModel.removeTag(index);
    this.targetChanged();
  }

  fixTagSegments() {
    // Adding tag with the same name as just removed works incorrectly if single segment is used (instead of array)
    this.addTagSegments = [this.uiSegmentSrv.newPlusButton()];
  }

  showDelimiter(index) {
    return index !== this.queryModel.tags.length - 1;
  }
}

function mapToDropdownOptions(results) {
  return _.map(results, (value) => {
    return {text: value, value: value};
  });
}

function removeTagPrefix(value: string): string {
  return value.replace(TAG_PREFIX, '');
}
