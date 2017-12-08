package self

import (
	"libbeat/common"
	"libbeat/common/cfgwarn"
	"metricbeat/helper"
	"metricbeat/mb"
	"metricbeat/mb/parse"
)

const (
	defaultScheme = "http"
	defaultPath   = "/v2/stats/self"
)

var (
	hostParser = parse.URLHostParserBuilder{
		DefaultScheme: defaultScheme,
		DefaultPath:   defaultPath,
	}.Build()
)

func init() {
	if err := mb.Registry.AddMetricSet("etcd", "self", New, hostParser); err != nil {
		panic(err)
	}
}

type MetricSet struct {
	mb.BaseMetricSet
	http *helper.HTTP
}

func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	cfgwarn.Experimental("The etcd self metricset is experimental")
	config := struct{}{}

	if err := base.Module().UnpackConfig(&config); err != nil {
		return nil, err
	}

	return &MetricSet{
		base,
		helper.NewHTTP(base),
	}, nil
}

func (m *MetricSet) Fetch() (common.MapStr, error) {
	content, err := m.http.FetchContent()
	if err != nil {
		return nil, err
	}
	return eventMapping(content), nil
}
