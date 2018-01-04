// +build darwin freebsd linux windows

package network

import (
	"strings"

	"libbeat/common"
	"libbeat/logp"
	"metricbeat/mb"
	"metricbeat/mb/parse"

	"github.com/pkg/errors"
	"github.com/shirou/gopsutil/net"
)

var debugf = logp.MakeDebug("system-network")

func init() {
	if err := mb.Registry.AddMetricSet("system", "network", New, parse.EmptyHostParser); err != nil {
		panic(err)
	}
}

// MetricSet for fetching system network IO metrics.
type MetricSet struct {
	mb.BaseMetricSet
	interfaces map[string]struct{}
}

// New is a mb.MetricSetFactory that returns a new MetricSet.
func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	// Unpack additional configuration options.
	config := struct {
		Interfaces []string `config:"interfaces"`
	}{}
	err := base.Module().UnpackConfig(&config)
	if err != nil {
		return nil, err
	}

	var interfaceSet map[string]struct{}
	if len(config.Interfaces) > 0 {
		interfaceSet = make(map[string]struct{}, len(config.Interfaces))
		for _, ifc := range config.Interfaces {
			interfaceSet[strings.ToLower(ifc)] = struct{}{}
		}
		debugf("network io stats will be included for %v", interfaceSet)
	}

	return &MetricSet{
		BaseMetricSet: base,
		interfaces:    interfaceSet,
	}, nil
}

// Fetch fetches network IO metrics from the OS.
func (m *MetricSet) Fetch() ([]common.MapStr, error) {
	stats, err := net.IOCounters(true)
	if err != nil {
		return nil, errors.Wrap(err, "network io counters")
	}

	var events []common.MapStr
	if m.interfaces == nil {
		// Include all stats.
		for _, counters := range stats {
			events = append(events, ioCountersToMapStr(counters))
		}
	} else {
		// Select stats by interface name.
		for _, counters := range stats {
			name := strings.ToLower(counters.Name)
			if _, include := m.interfaces[name]; include {
				events = append(events, ioCountersToMapStr(counters))
				continue
			}
		}
	}

	return events, nil
}

func ioCountersToMapStr(counters net.IOCountersStat) common.MapStr {
	return common.MapStr{
		"name_string": counters.Name,
		"in": common.MapStr{
			"errors_long":  counters.Errin,
			"dropped_long": counters.Dropin,
			"bytes_long":   counters.BytesRecv,
			"packets_long": counters.PacketsRecv,
		},
		"out": common.MapStr{
			"errors_long":  counters.Errout,
			"dropped_long": counters.Dropout,
			"packets_long": counters.PacketsSent,
			"bytes_long":   counters.BytesSent,
		},
	}
}
