// +build darwin freebsd linux openbsd windows

// +build darwin freebsd linux openbsd windows

package memory

import (
	"libbeat/common"
	"metricbeat/mb"
	"metricbeat/mb/parse"

	"github.com/pkg/errors"
)

func init() {
	if err := mb.Registry.AddMetricSet("system", "memory", New, parse.EmptyHostParser); err != nil {
		panic(err)
	}
}

// MetricSet for fetching system memory metrics.
type MetricSet struct {
	mb.BaseMetricSet
}

// New is a mb.MetricSetFactory that returns a memory.MetricSet.
func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	return &MetricSet{base}, nil
}

// Fetch fetches memory metrics from the OS.
func (m *MetricSet) Fetch() (event common.MapStr, err error) {
	memStat, err := GetMemory()
	if err != nil {
		return nil, errors.Wrap(err, "memory")
	}
	AddMemPercentage(memStat)

	swapStat, err := GetSwap()
	if err != nil {
		return nil, errors.Wrap(err, "swap")
	}
	AddSwapPercentage(swapStat)

	memory := common.MapStr{
		"total_long": memStat.Total,
		"used": common.MapStr{
			"bytes_long": memStat.Used,
			"pct_double": memStat.UsedPercent,
		},
		"free_long": memStat.Free,
		"actual": common.MapStr{
			"free_long": memStat.ActualFree,
			"used": common.MapStr{
				"pct_double": memStat.ActualUsedPercent,
				"bytes_long": memStat.ActualUsed,
			},
		},
	}

	swap := common.MapStr{
		"total_long": swapStat.Total,
		"used": common.MapStr{
			"bytes_long": swapStat.Used,
			"pct_double": swapStat.UsedPercent,
		},
		"free_long": swapStat.Free,
	}

	memory["swap_long"] = swap
	return memory, nil
}
