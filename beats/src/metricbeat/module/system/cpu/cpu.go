// +build darwin freebsd linux openbsd windows

package cpu

import (
	"strings"

	"github.com/pkg/errors"

	"libbeat/common"
	"metricbeat/mb"
	"metricbeat/mb/parse"
	"metricbeat/module/system"
)

func init() {
	if err := mb.Registry.AddMetricSet("system", "cpu", New, parse.EmptyHostParser); err != nil {
		panic(err)
	}
}

// MetricSet for fetching system CPU metrics.
type MetricSet struct {
	mb.BaseMetricSet
	config Config
	cpu    *system.CPUMonitor
}

// New is a mb.MetricSetFactory that returns a cpu.MetricSet.
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
		cpu:           new(system.CPUMonitor),
	}, nil
}

// Fetch fetches CPU metrics from the OS.
func (m *MetricSet) Fetch() (common.MapStr, error) {
	sample, err := m.cpu.Sample()
	if err != nil {
		return nil, errors.Wrap(err, "failed to fetch CPU times")
	}

	event := common.MapStr{"cores_long_tag": system.NumCPU}

	for _, metric := range m.config.Metrics {
		switch strings.ToLower(metric) {
		case percentages:
			pct := sample.Percentages()
			event.Put("user.pct_double", pct.User)
			event.Put("system.pct_double", pct.System)
			event.Put("idle.pct_double", pct.Idle)
			event.Put("iowait.pct_double", pct.IOWait)
			event.Put("irq.pct_double", pct.IRQ)
			event.Put("nice.pct_double", pct.Nice)
			event.Put("softirq.pct_double", pct.SoftIRQ)
			event.Put("steal.pct_double", pct.Steal)
			event.Put("total.pct_double", pct.Total)
		case normalizedPercentages:
			normalizedPct := sample.NormalizedPercentages()
			event.Put("user.norm.pct_double", normalizedPct.User)
			event.Put("system.norm.pct_double", normalizedPct.System)
			event.Put("idle.norm.pct_double", normalizedPct.Idle)
			event.Put("iowait.norm.pct_double", normalizedPct.IOWait)
			event.Put("irq.norm.pct_double", normalizedPct.IRQ)
			event.Put("nice.norm.pct_double", normalizedPct.Nice)
			event.Put("softirq.norm.pct_double", normalizedPct.SoftIRQ)
			event.Put("steal.norm.pct_double", normalizedPct.Steal)
			event.Put("total.norm.pct_double", normalizedPct.Total)
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

	return event, nil
}
