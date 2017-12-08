// +build integration

package stats

import (
	"os"
	"testing"

	"libbeat/tests/compose"
	mbtest "metricbeat/mb/testing"
)

func TestData(t *testing.T) {
	compose.EnsureUp(t, "memcached")

	f := mbtest.NewEventFetcher(t, getConfig())
	err := mbtest.WriteEvent(f, t)
	if err != nil {
		t.Fatal("write", err)
	}
}

func getConfig() map[string]interface{} {
	return map[string]interface{}{
		"module":     "memcached",
		"metricsets": []string{"stats"},
		"hosts":      []string{getEnvHost() + ":" + getEnvPort()},
	}
}

func getEnvHost() string {
	host := os.Getenv("MEMCACHED_HOST")

	if len(host) == 0 {
		host = "127.0.0.1"
	}
	return host
}

func getEnvPort() string {
	port := os.Getenv("MEMCACHED_PORT")

	if len(port) == 0 {
		port = "11211"
	}
	return port
}
