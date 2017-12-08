// +build integration

package healthcheck

import (
	"testing"

	mbtest "metricbeat/mb/testing"
)

func TestData(t *testing.T) {
	f := mbtest.NewEventsFetcher(t, getConfig())
	err := mbtest.WriteEvents(f, t)
	if err != nil {
		t.Fatal("write", err)
	}
}

func getConfig() map[string]interface{} {
	return map[string]interface{}{
		"module":     "docker",
		"metricsets": []string{"healthcheck"},
		"hosts":      []string{"unix:///var/run/docker.sock"},
	}
}
