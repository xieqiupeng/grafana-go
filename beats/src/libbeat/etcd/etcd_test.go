package etcd

import (
	"encoding/json"
	"fmt"
	"testing"
	"time"

	"github.com/coreos/etcd/clientv3"
)

func CreateEtcdClientConfig() (etcdConfig clientv3.Config, configPath string) {

	etcdConfig = clientv3.Config{
		Endpoints:   []string{"10.100.139.150:2379", "10.100.139.151:2379", "10.100.139.153:2379"},
		DialTimeout: 5 * time.Second,
	}
	configPath = "/prospectorconfig"

	return
}

func TestWatchDog(t *testing.T) {
	cfg, path := CreateEtcdClientConfig()
	c := NewEtcdClient(cfg, path)
	c.GetAllConfig()
}

func TestPutEtcd(t *testing.T) {

	cfg, path := CreateEtcdClientConfig()

	c := NewEtcdClient(cfg, path)

	//c.Put("/prospectorconfig/a", "{\"ipAddress\": [\"10.100.139.150\",\"10.100.139.151\"],\"path\": [\"/app/testLog.log\"],\"type\": \"log\"}")

	//c.Put("/prospectorconfig/b", "{\"ipAddress\": [\"10.100.139.150\",\"10.100.139.151\"],\"path\": [\"/app/testLog.log\"],\"type\": \"log\"}")

	//c.Put("/prospectorconfig/c", "{\"ipAddress\": [\"10.100.139.150\",\"10.100.139.151\"],\"path\": [\"/app/testLog.log\"],\"type\": \"log\"}")

	//c.Put("/prospectorconfig/a", "{\"ipAddress\": [\"10.100.139.150\",\"10.100.139.151\"],\"path\": [\"/app/testLog.log\"],\"type\": \"log\"}")

	//c.Put("/prospectorconfig/b", "{\"ipAddress\": [\"10.100.139.150\",\"10.100.139.151\"],\"path\": [\"/app/testLog.log\"],\"type\": \"log\"}")

	c.Put("/prospectorconfig/c", "{\"ipAddress\": [\"10.10.180.66\",\"10.100.139.151\"],\"path\": [\"/Users/creditease/bingo/test.log\"],\"type\": \"log\"}")

	//c.Delete("/prospectorconfig/a")

	//c.Delete("/prospectorconfig/b")

	//c.Delete("/prospectorconfig/c")

	//c.GetAllConfig()
}

func TestNewEtcdClient(t *testing.T) {
	cf := ProspectorConfig{
		T:         "log",
		Enabled:   true,
		Paths:     []string{"/Users/creditease/bingo/test.log", "/Users/creditease/bingo/test.log"},
		TailFiles: true,
		Fields: map[string]string{
			"tag1": "AAAAA",
			"tag2": "BBBBB",
		},
	}

	s, _ := json.Marshal(cf)
	fmt.Println(string(s))
	d, err := json.Marshal([]ProspectorConfig{cf, cf, cf})
	if err != nil {
		fmt.Errorf(err.Error())
	}
	fmt.Println("--------------")
	fmt.Println(string(d))
	fmt.Println("--------------")
	cfg, path := CreateEtcdClientConfig()
	c := NewEtcdClient(cfg, path)
	c.Put("prospectorconfig", string(d))
	conf, _ := c.GetAllConfig()

	fmt.Println(len(conf))
	//c.WatchDog()
}

func TestEtcdConfig(t *testing.T) {
	c := ProspectorConfig{
		T:         "log",
		Enabled:   true,
		Paths:     []string{"", ""},
		TailFiles: true,
		Fields: map[string]string{
			"tag1": "AAAAA",
			"tag2": "BBBBB",
		},
	}

	s, _ := json.Marshal(c)
	fmt.Println(string(s))
	d, err := json.Marshal([]ProspectorConfig{c, c, c})
	if err != nil {
		fmt.Errorf(err.Error())
	}
	fmt.Println("--------------")
	fmt.Println(string(d))
	fmt.Println("--------------")

	var rs []ProspectorConfig

	json.Unmarshal(d, &rs)
	fmt.Println(len(rs))
}
