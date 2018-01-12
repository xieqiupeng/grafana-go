// +build darwin,cgo freebsd linux windows

package diskio

import (
	"libbeat/common"
	"metricbeat/mb"
	"metricbeat/mb/parse"

	"github.com/pkg/errors"
	"github.com/shirou/gopsutil/disk"
)

func init() {
	if err := mb.Registry.AddMetricSet("system", "diskio", New, parse.EmptyHostParser); err != nil {
		panic(err)
	}
}

// MetricSet for fetching system disk IO metrics.
type MetricSet struct {
	mb.BaseMetricSet
	statistics *DiskIOStat
}

// New is a mb.MetricSetFactory that returns a new MetricSet.
func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	ms := &MetricSet{
		BaseMetricSet: base,
		statistics:    NewDiskIOStat(),
	}
	return ms, nil
}

// Fetch fetches disk IO metrics from the OS.
func (m *MetricSet) Fetch() ([]common.MapStr, error) {
	stats, err := disk.IOCounters()
	if err != nil {
		return nil, errors.Wrap(err, "disk io counters")
	}

	// open a sampling means sample the current cpu counter
	m.statistics.OpenSampling()

	events := make([]common.MapStr, 0, len(stats))
	for _, counters := range stats {

		event := common.MapStr{
			"name_string_tag": counters.Name,
			"read": common.MapStr{
				"count_long": counters.ReadCount,
				"time_long":  counters.ReadTime,
				"bytes_long": counters.ReadBytes,
			},
			"write": common.MapStr{
				"count_long": counters.WriteCount,
				"time_long":  counters.WriteTime,
				"bytes_long": counters.WriteBytes,
			},
			"io": common.MapStr{
				"time_long": counters.IoTime,
			},
		}

		extraMetrics, err := m.statistics.CalIOStatistics(counters)
		if err == nil {
			event["iostat"] = common.MapStr{
				"read": common.MapStr{
					"request": common.MapStr{
						"merges_per_sec_float": extraMetrics.ReadRequestMergeCountPerSec,
						"per_sec_float":        extraMetrics.ReadRequestCountPerSec,
					},
					"per_sec": common.MapStr{
						"bytes_float": extraMetrics.ReadBytesPerSec,
					},
				},
				"write": common.MapStr{
					"request": common.MapStr{
						"merges_per_sec_float": extraMetrics.WriteRequestMergeCountPerSec,
						"per_sec_float":        extraMetrics.WriteRequestCountPerSec,
					},
					"per_sec": common.MapStr{
						"bytes_float": extraMetrics.WriteBytesPerSec,
					},
				},
				"queue": common.MapStr{
					"avg_size_float": extraMetrics.AvgQueueSize,
				},
				"request": common.MapStr{
					"avg_size_float": extraMetrics.AvgRequestSize,
				},
				"await_float":        extraMetrics.AvgAwaitTime,
				"service_time_float": extraMetrics.AvgServiceTime,
				"busy_float":         extraMetrics.BusyPct,
			}
		}

		events = append(events, event)

		if counters.SerialNumber != "" {
			event["serial_number_string_tag"] = counters.SerialNumber
		}
	}

	// open a sampling means store the last cpu counter
	m.statistics.CloseSampling()

	return events, nil
}
