// +build darwin freebsd linux openbsd

package load

import (
	"github.com/pkg/errors"

	"libbeat/common"
	"metricbeat/mb"
	"metricbeat/mb/parse"
	"metricbeat/module/system"
)

func init() {
	if err := mb.Registry.AddMetricSet("system", "load", New, parse.EmptyHostParser); err != nil {
		panic(err)
	}
}

// MetricSet for fetching system CPU load metrics.
type MetricSet struct {
	mb.BaseMetricSet
}

// New returns a new load MetricSet.
func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	return &MetricSet{
		BaseMetricSet: base,
	}, nil
}

// Fetch fetches system load metrics.
func (m *MetricSet) Fetch() (common.MapStr, error) {
	load, err := system.Load()
	if err != nil {
		return nil, errors.Wrap(err, "failed to get CPU load values")
	}

	avgs := load.Averages()
	normAvgs := load.NormalizedAverages()

	event := common.MapStr{
		"cores_long":     system.NumCPU,
		"one_double":     avgs.OneMinute,
		"five_double":    avgs.FiveMinute,
		"fifteen_double": avgs.FifteenMinute,
		"norm": common.MapStr{
			"one_double":     normAvgs.OneMinute,
			"five_double":    normAvgs.FiveMinute,
			"fifteen_double": normAvgs.FifteenMinute,
		},
	}

	return event, nil
}
