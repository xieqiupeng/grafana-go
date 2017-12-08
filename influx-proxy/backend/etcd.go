package backend

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/coreos/etcd/clientv3"
	"log"
	"time"
)

var (
	requestTimeout = 10 * time.Second
)

type EtcdConfig struct {
	Dfn   map[string]string            `json:"dfn"`
	Bks   map[string]map[string]string `json:"bks"`
	Nodes map[string]map[string]string `json:"nodes"`
	Kmps  map[string][]string          `json:"kmps"`
}

type EtcdClient struct {
	client *clientv3.Client
	path   string
}

func NewEtcdClient(etcdConfig clientv3.Config, configPath string) (etcdClient *EtcdClient) {

	cli, err := clientv3.New(etcdConfig)
	if err != nil {
		log.Fatalln(err)
	}
	etcdClient = &EtcdClient{
		client: cli,
		path:   configPath,
	}
	return
}

func (c *EtcdClient) getAllConfig() (config *EtcdConfig, error *error) {
	ctx, cancel := context.WithTimeout(context.Background(), requestTimeout)
	rsp, err := c.client.Get(ctx, c.path)
	cancel()
	if err != nil {
		log.Fatalln(err)
	}

	for _, ev := range rsp.Kvs {
		fmt.Printf("%s : %s\n", ev.Key, ev.Value)
		if ev.Value != nil {
			json.Unmarshal(ev.Value, config)
		}
	}
	return
}

func (c *EtcdClient) watchDog() {
	rch := c.client.Watch(context.Background(), c.path, clientv3.WithPrefix(), clientv3.WithKeysOnly())
	for rsp := range rch {
		for _, ev := range rsp.Events {
			fmt.Printf("%s %s : %s\n", ev.Type, ev.Kv.Key, ev.Kv.Value)
		}
	}
}
