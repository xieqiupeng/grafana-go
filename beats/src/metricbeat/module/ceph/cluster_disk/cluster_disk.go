package cluster_disk

import (
	"libbeat/common"
	"libbeat/common/cfgwarn"
	"metricbeat/helper"
	"metricbeat/mb"
	"metricbeat/mb/parse"
)

const (
	defaultScheme = "http"
	defaultPath   = "/api/v0.1/df"
)

var (
	hostParser = parse.URLHostParserBuilder{
		DefaultScheme: defaultScheme,
		DefaultPath:   defaultPath,
	}.Build()
)

func init() {
	if err := mb.Registry.AddMetricSet("ceph", "cluster_disk", New, hostParser); err != nil {
		panic(err)
	}
}

type MetricSet struct {
	mb.BaseMetricSet
	*helper.HTTP
}

func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	cfgwarn.Experimental("The ceph cluster_disk metricset is experimental")

	http := helper.NewHTTP(base)
	http.SetHeader("Accept", "application/json")

	return &MetricSet{
		base,
		http,
	}, nil
}

func (m *MetricSet) Fetch() (common.MapStr, error) {
	content, err := m.HTTP.FetchContent()

	if err != nil {
		return nil, err
	}

	return eventMapping(content), nil
}
