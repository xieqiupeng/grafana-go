package backend

import (
	"encoding/json"
	"fmt"
	"github.com/coreos/etcd/clientv3"
	"testing"
	"time"
)

func CreateEtcdClientConfig() (etcdConfig clientv3.Config, configPath string) {

	etcdConfig = clientv3.Config{
		Endpoints:   []string{"127.0.0.1:8001", "127.0.0.1:8002", "127.0.0.1:8003"},
		DialTimeout: 5 * time.Second,
	}
	configPath = "etcd_test"

	return
}

func TestNewEtcdClient(t *testing.T) {
	cfg, path := CreateEtcdClientConfig()
	c := NewEtcdClient(cfg, path)
	c.getAllConfig()
	c.watchDog()
}

func TestEtcdConfig(t *testing.T) {
	c := EtcdConfig{
		Dfn: map[string]string{
			"listenaddr": ":6666",
		},
		Bks: map[string]map[string]string{
			"local": map[string]string{
				"url":             "http://localhost:8086",
				"db":              "test",
				"zone":            "local",
				"interval":        "1000",
				"timeout":         "10000",
				"timeoutquery":    "600000",
				"maxrowlimit":     "10000",
				"checkinterval":   "1000",
				"rewriteinterval": "10000",
			},
			"local2": map[string]string{
				"url":      "http://influxdb-test:8086",
				"db":       "test2",
				"interval": "200",
			},
		},
		Nodes: map[string]map[string]string{
			"l1": map[string]string{
				"listenaddr":   ":6666",
				"db":           "test",
				"zone":         "local",
				"interval":     "10",
				"idletimeout":  "10",
				"writetracing": "0",
				"querytracing": "0",
			},
		},
		Kmps: map[string][]string{
			"cpu":         []string{"local"},
			"temperature": []string{"local2"},
		},
	}

	d, err := json.Marshal(c)
	if err != nil {
		fmt.Errorf(err.Error())
	}
	fmt.Println("--------------")
	fmt.Println(string(d))
	fmt.Println("--------------")

	var rs EtcdConfig

	json.Unmarshal(d, &rs)
	fmt.Println(len(rs.Kmps))
}
