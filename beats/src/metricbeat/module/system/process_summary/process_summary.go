// +build darwin freebsd linux windows

package process_summary

import (
	"github.com/pkg/errors"

	"libbeat/common"
	"libbeat/logp"
	"metricbeat/mb"
	"metricbeat/mb/parse"
	"metricbeat/module/system/process"

	sigar "github.com/elastic/gosigar"
)

// init registers the MetricSet with the central registry.
// The New method will be called after the setup of the module and before starting to fetch data
func init() {
	if err := mb.Registry.AddMetricSet("system", "process_summary", New, parse.EmptyHostParser); err != nil {
		panic(err)
	}
}

// MetricSet type defines all fields of the MetricSet
// As a minimum it must inherit the mb.BaseMetricSet fields, but can be extended with
// additional entries. These variables can be used to persist data or configuration between
// multiple fetch calls.
type MetricSet struct {
	mb.BaseMetricSet
}

// New create a new instance of the MetricSet
// Part of new is also setting up the configuration by processing additional
// configuration entries if needed.
func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	return &MetricSet{
		BaseMetricSet: base,
	}, nil
}

// Fetch methods implements the data gathering and data conversion to the right format
// It returns the event which is then forward to the output. In case of an error, a
// descriptive error must be returned.
func (m *MetricSet) Fetch() (common.MapStr, error) {
	pids, err := process.Pids()
	if err != nil {
		return nil, errors.Wrap(err, "failed to fetch the list of PIDs")
	}

	var summary struct {
		sleeping int
		running  int
		idle     int
		stopped  int
		zombie   int
		unknown  int
	}

	for _, pid := range pids {
		state := sigar.ProcState{}
		err = state.Get(pid)
		if err != nil {
			summary.unknown += 1
			continue
		}

		switch byte(state.State) {
		case 'S':
			summary.sleeping++
		case 'R':
			summary.running++
		case 'D':
			summary.idle++
		case 'T':
			summary.stopped++
		case 'Z':
			summary.zombie++
		default:
			logp.Err("Unknown state <%v> for process with pid %d", state.State, pid)
			summary.unknown++
		}
	}

	event := common.MapStr{
		"total_long":    len(pids),
		"sleeping_long": summary.sleeping,
		"running_long":  summary.running,
		"idle_long":     summary.idle,
		"stopped_long":  summary.stopped,
		"zombie_long":   summary.zombie,
		"unknown_long":  summary.unknown,
	}
	// change the name space to use . instead of _
	event[mb.NamespaceKey+"_string"] = "process.summary"

	return event, nil
}
