package ctr

import (
	"fmt"
	"libbeat/etcd"
	"os"
	"os/exec"
	"path/filepath"
	"time"

	"github.com/coreos/etcd/clientv3"
)

type BeatCtr struct {
	etcd    *etcd.EtcdClient
	modules map[string][]string
}

func New(endpoints []string, dialtimeout time.Duration, username string, password string, key string, mds []map[string]string) (bt *BeatCtr, err error) {

	etcdCfg := clientv3.Config{
		Endpoints:   endpoints,
		Username:    username,
		Password:    password,
		DialTimeout: dialtimeout * time.Second,
	}

	etcd := etcd.NewEtcdClient(etcdCfg, key)
	modules := make(map[string][]string)
	for _, m := range mds {
		name, _ := m["name"]
		conf, _ := m["conf"]

		modules[name] = []string{"-c", conf}

	}
	bt = &BeatCtr{
		etcd:    etcd,
		modules: modules,
	}
	return bt, nil
}

func (bt *BeatCtr) Run() (error *error) {

	cfg, err := bt.etcd.GetAllConfig()

	//etcd 有本机相关配置启动相应beat
	if cfg != nil && len(cfg) > 0 {
		fmt.Println("start beats ...")
		bt.forkProcess()
	} else {
		//etcd 没有本机相关配置等待配置下发后在启动相应beat

		dogChan := make(chan *clientv3.Event)

		go bt.etcd.WatchDog(dogChan)

		go bt.startBeats(dogChan)
	}

	return err
}

func (bt *BeatCtr) startBeats(chan *clientv3.Event) (error *error) {

	return nil
}

func (bt *BeatCtr) forkProcess() {
	if os.Getpid() != 1 {
		for k, v := range bt.modules {
			p, _ := filepath.Abs(k)
			cmd := exec.Command(p, v...)
			cmd.Stdin = os.Stdin
			cmd.Stdout = os.Stdout
			cmd.Stderr = os.Stderr
			err := cmd.Start()
			if err == nil {
				fmt.Printf(k+":启动成功...:%v", cmd.Process.Pid)
			} else {
				panic(err)
			}
		}

	}
}
