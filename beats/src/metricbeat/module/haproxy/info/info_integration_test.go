// +build integration

package info

import (
	"testing"

	mbtest "metricbeat/mb/testing"
	"metricbeat/module/haproxy"
)

func TestData(t *testing.T) {
	f := mbtest.NewEventFetcher(t, getConfig())
	err := mbtest.WriteEvent(f, t)
	if err != nil {
		t.Fatal("write", err)
	}
}

func getConfig() map[string]interface{} {
	return map[string]interface{}{
		"module":     "haproxy",
		"metricsets": []string{"info"},
		"hosts":      []string{"tcp://" + haproxy.GetEnvHost() + ":" + haproxy.GetEnvPort()},
	}
}
