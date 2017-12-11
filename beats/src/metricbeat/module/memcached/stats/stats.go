package stats

import (
	"bufio"
	"net"
	"strings"

	"libbeat/common"
	"libbeat/common/cfgwarn"
	"metricbeat/mb"
)

func init() {
	if err := mb.Registry.AddMetricSet("memcached", "stats", New); err != nil {
		panic(err)
	}
}

type MetricSet struct {
	mb.BaseMetricSet
}

func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	cfgwarn.Beta("The memcached stats metricset is beta")

	return &MetricSet{
		BaseMetricSet: base,
	}, nil
}

func (m *MetricSet) Fetch() (common.MapStr, error) {
	conn, err := net.DialTimeout("tcp", m.Host(), m.Module().Config().Timeout)
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	_, err = conn.Write([]byte("stats\n"))
	if err != nil {
		return nil, err
	}

	scanner := bufio.NewScanner(conn)

	data := map[string]interface{}{}

	for scanner.Scan() {
		text := scanner.Text()
		if text == "END" {
			break
		}

		// Split entries which look like: STAT time 1488291730
		entries := strings.Split(text, " ")
		if len(entries) == 3 {
			data[entries[1]] = entries[2]
		}
	}

	event, _ := schema.Apply(data)
	return event, nil
}
