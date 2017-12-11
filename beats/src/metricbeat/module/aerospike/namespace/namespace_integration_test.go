// +build integration

package namespace

import (
	"testing"

	"libbeat/tests/compose"
	mbtest "metricbeat/mb/testing"
	"metricbeat/module/aerospike"
)

func TestData(t *testing.T) {
	compose.EnsureUp(t, "aerospike")

	f := mbtest.NewEventsFetcher(t, getConfig())
	err := mbtest.WriteEvents(f, t)
	if err != nil {
		t.Fatal("write", err)
	}
}

func getConfig() map[string]interface{} {
	return map[string]interface{}{
		"module":     "aerospike",
		"metricsets": []string{"namespace"},
		"hosts":      []string{aerospike.GetAerospikeEnvHost() + ":" + aerospike.GetAerospikeEnvPort()},
	}
}
