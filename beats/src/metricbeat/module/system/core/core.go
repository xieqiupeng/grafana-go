// +build darwin freebsd linux openbsd windows

package core

import (
	"strings"

	"github.com/pkg/errors"

	"libbeat/common"
	"metricbeat/mb"
	"metricbeat/mb/parse"
	"metricbeat/module/system"
)

func init() {
	if err := mb.Registry.AddMetricSet("system", "core", New, parse.EmptyHostParser); err != nil {
		panic(err)
	}
}

// MetricSet for fetching system core metrics.
type MetricSet struct {
	mb.BaseMetricSet
	config Config
	cores  *system.CPUCoresMonitor
}

// New returns a new core MetricSet.
func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	config := defaultConfig
	if err := base.Module().UnpackConfig(&config); err != nil {
		return nil, err
	}

	if config.CPUTicks != nil && *config.CPUTicks {
		config.Metrics = append(config.Metrics, "ticks")
	}

	return &MetricSet{
		BaseMetricSet: base,
		config:        config,
		cores:         new(system.CPUCoresMonitor),
	}, nil
}

// Fetch fetches CPU core metrics from the OS.
func (m *MetricSet) Fetch(report mb.Reporter) {
	samples, err := m.cores.Sample()
	if err != nil {
		report.Error(errors.Wrap(err, "failed to sample CPU core times"))
		return
	}

	for id, sample := range samples {
		event := common.MapStr{"id_long": id}

		for _, metric := range m.config.Metrics {
			switch strings.ToLower(metric) {
			case percentages:
				// Use NormalizedPercentages here because per core metrics range on [0, 100%].
				pct := sample.Percentages()
				event.Put("user.pct_double", pct.User)
				event.Put("system.pct_double", pct.System)
				event.Put("idle.pct_double", pct.Idle)
				event.Put("iowait.pct_double", pct.IOWait)
				event.Put("irq.pct_double", pct.IRQ)
				event.Put("nice.pct_double", pct.Nice)
				event.Put("softirq.pct_double", pct.SoftIRQ)
				event.Put("steal.pct_double", pct.Steal)
			case ticks:
				ticks := sample.Ticks()
				event.Put("user.ticks_long", ticks.User)
				event.Put("system.ticks_long", ticks.System)
				event.Put("idle.ticks_long", ticks.Idle)
				event.Put("iowait.ticks_long", ticks.IOWait)
				event.Put("irq.ticks_long", ticks.IRQ)
				event.Put("nice.ticks_long", ticks.Nice)
				event.Put("softirq.ticks_long", ticks.SoftIRQ)
				event.Put("steal.ticks_long", ticks.Steal)
			}
		}

		report.Event(event)
	}
}
