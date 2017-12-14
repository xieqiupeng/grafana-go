package etcd

import (
	"context"
	"encoding/json"
	"fmt"
	"libbeat/common"
	"log"
	"net"
	"strings"
	"time"

	"github.com/coreos/etcd/clientv3"
	"github.com/coreos/etcd/mvcc/mvccpb"
	"github.com/elastic/go-ucfg"
)

var (
	requestTimeout = 10 * time.Second

	configOpts = []ucfg.Option{
		ucfg.PathSep("."),
		ucfg.ResolveEnv,
		ucfg.VarExp,
	}
)

type ProspectorConfig struct {
	T         string            `json:"type"`
	Enabled   bool              `json:"enabled"`
	Paths     []string          `json:"paths"`
	TailFiles bool              `json:"tail_files"`
	Fields    map[string]string `json:"fields"`
	Key       string            `json:"key"`
}

type EtcdClient struct {
	client *clientv3.Client
	path   string
	keys   map[string]bool
}

func GetLocalHostIp() (ip string, err error) {

	addrs, err := net.InterfaceAddrs()
	if err != nil {
		log.Fatalln(err)
		return "", err
	}
	for _, addr := range addrs {
		if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if nil != ipnet.IP.To4() {
				ip = ipnet.IP.String()
				return ip, nil
			}
		}
	}
	return ip, nil
}

func Convert2Strings(s []interface{}) (t []string) {
	for _, v := range s {
		t = append(t, v.(string))
	}
	return t
}

func NewEtcdClient(etcdConfig clientv3.Config, configPath string) (etcdClient *EtcdClient) {

	cli, err := clientv3.New(etcdConfig)
	if err != nil {
		log.Fatalln(err)
	}
	etcdClient = &EtcdClient{
		client: cli,
		path:   configPath,
		keys:   make(map[string]bool),
	}
	return
}

func (c *EtcdClient) Put(k string, v string) (err error) {
	ctx, cancel := context.WithTimeout(context.Background(), requestTimeout)
	rsp, err := c.client.Put(ctx, k, v)
	fmt.Println(rsp)
	cancel()
	if err != nil {
		log.Fatalln(err)

	}
	return err
}

func (c *EtcdClient) Delete(k string) (err error) {
	ctx, cancel := context.WithTimeout(context.Background(), requestTimeout)
	rsp, err := c.client.Delete(ctx, k)
	fmt.Println(rsp)
	cancel()
	if err != nil {
		log.Fatalln(err)

	}
	return err
}

func (c *EtcdClient) GetAllConfig() (config []*common.Config, error *error) {
	ctx, cancel := context.WithTimeout(context.Background(), requestTimeout)
	rsp, err := c.client.Get(ctx, c.path, clientv3.WithPrefix())
	cancel()
	if err != nil {
		log.Fatalln(err)
		return nil, &err
	}
	ip, e := GetLocalHostIp()
	if e != nil || ip == "" {
		log.Fatalln(e)
		return nil, &e
	}
	for _, ev := range rsp.Kvs {
		fmt.Printf("%s : %s\n", ev.Key, ev.Value)
		if ev.Value != nil {
			var v map[string]interface{}
			e := json.Unmarshal(ev.Value, &v)
			if e != nil {
				log.Fatalln(e)
				break
			}
			ipAddress := Convert2Strings(v["ipAddress"].([]interface{}))
			if !strings.Contains(strings.Join(ipAddress, "^"), ip) {
				//跳过不适用本机的规则
				continue
			}
			items := strings.Split(string(ev.Key), "/")
			f := items[len(items)-1]
			cfg := &ProspectorConfig{
				T:         "log",
				Enabled:   true,
				Paths:     Convert2Strings(v["path"].([]interface{})),
				TailFiles: true,
				Fields: map[string]string{
					"tag": f,
				},
				Key: string(ev.Key),
			}
			cf, e := common.NewConfigFrom(cfg)
			if e == nil {
				config = append(config, cf)
				c.keys[string(ev.Key)] = true
			} else {
				log.Fatalln(e)
			}
		}
	}
	return config, nil
}

func (c *EtcdClient) WatchDog(ch chan *clientv3.Event) {
	rch := c.client.Watch(context.Background(), c.path, clientv3.WithPrefix())
	ip, e := GetLocalHostIp()
	if e != nil || ip == "" {
		log.Fatalln(e)
		return
	}
	for rsp := range rch {

		for _, ev := range rsp.Events {

			log.Printf("%s %s : %s\n", ev.Type, ev.Kv.Key, ev.Kv.Value)

			switch ev.Type {

			case mvccpb.DELETE:
				_, ok := c.keys[string(ev.Kv.Key)]
				if ok {
					delete(c.keys, string(ev.Kv.Key))
					ch <- ev
				}
			case mvccpb.PUT:
				if ev.Kv.Value != nil {
					var v map[string]interface{}
					e := json.Unmarshal(ev.Kv.Value, &v)
					if e != nil {
						log.Fatalln(e)
						continue
					}
					ipAddress := Convert2Strings(v["ipAddress"].([]interface{}))
					// 针对修改配置将制定host删除
					if _, ok := c.keys[string(ev.Kv.Key)]; ok && !strings.Contains(strings.Join(ipAddress, "^"), ip) {
						e := &clientv3.Event{
							Type: mvccpb.DELETE,
							Kv: &mvccpb.KeyValue{
								Key: ev.Kv.Key,
							},
						}
						delete(c.keys, string(ev.Kv.Key))
						ch <- e
					}
					if strings.Contains(strings.Join(ipAddress, "^"), ip) {
						//只处理适用本机规则
						c.keys[string(ev.Kv.Key)] = true
						ch <- ev
					}
				}
			}

		}
	}
}
