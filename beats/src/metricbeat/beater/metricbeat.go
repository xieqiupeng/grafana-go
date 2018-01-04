package beater

import (
	"fmt"
	"libbeat/etcd"
	"sync"
	"time"

	"github.com/coreos/etcd/clientv3"
	"github.com/coreos/etcd/mvcc/mvccpb"
	"github.com/joeshaw/multierror"
	"github.com/pkg/errors"

	"libbeat/beat"
	"libbeat/cfgfile"
	"libbeat/common"
	"libbeat/common/cfgwarn"
	"libbeat/logp"
	"metricbeat/mb"
	"metricbeat/mb/module"

	// Add metricbeat specific processors
	_ "metricbeat/processor/add_kubernetes_metadata"
)

// Metricbeat implements the Beater interface for metricbeat.
type Metricbeat struct {
	done       map[string]chan struct{} // Channel used to initiate shutdown.
	modules    []staticModule           // Active list of modules.
	config     Config
	etcd       *etcd.EtcdClient
	reloadDone chan struct{}
	beat       *beat.Beat
}

type EtcdConifg struct {
	Config     clientv3.Config `config:"config"`
	ConfigPath string          `config:"configpath"`
}

type staticModule struct {
	connector *module.Connector
	module    *module.Wrapper
}

// New creates and returns a new Metricbeat instance.
func New(b *beat.Beat, rawConfig *common.Config) (beat.Beater, error) {

	// List all registered modules and metricsets.
	logp.Debug("modules", "%s", mb.Registry.String())

	config := defaultConfig
	if err := rawConfig.Unpack(&config); err != nil {
		return nil, errors.Wrap(err, "error reading configuration file")
	}

	dynamicCfgEnabled := config.ConfigModules.Enabled()
	if !dynamicCfgEnabled && len(config.Modules) == 0 {
		return nil, mb.ErrEmptyConfig
	}
	//创建ETCD客户端并获取全量配置

	etcdConfig := &EtcdConifg{}

	cs, _ := rawConfig.Child("etcd", -1)
	cs.Unpack(&etcdConfig)
	etcd := etcd.NewEtcdClient(etcdConfig.Config, etcdConfig.ConfigPath)

	conf, e := FetchConfigsWithEtcd(etcd)
	if e != nil {
		logp.Err("从ETCD获取配置异常:%s", e)
	}

	var errs multierror.Errors
	var modules []staticModule

	done := make(map[string]chan struct{})

	for _, moduleCfg := range config.Modules {
		if !moduleCfg.Enabled() {
			continue
		}
		if k, _ := moduleCfg.String("module", -1); k == "jolokia" {
			if moduleCfg.HasField("hosts") && conf != nil {
				moduleCfg.SetChild("hosts", -1, conf)
			} else {
				continue
			}

		}
		failed := false

		err := cfgwarn.CheckRemoved5xSettings(moduleCfg, "filters")
		if err != nil {
			errs = append(errs, err)
			failed = true
		}

		connector, err := module.NewConnector(b.Publisher, moduleCfg)
		if err != nil {
			errs = append(errs, err)
			failed = true
		}

		module, err := module.NewWrapper(config.MaxStartDelay, moduleCfg, mb.Registry)
		if err != nil {
			errs = append(errs, err)
			failed = true
		}

		if failed {
			continue
		}

		modules = append(modules, staticModule{
			connector: connector,
			module:    module,
		})
		done[module.Name()] = make(chan struct{})
	}

	if err := errs.Err(); err != nil {
		return nil, err
	}
	if len(modules) == 0 && !dynamicCfgEnabled {
		return nil, mb.ErrAllModulesDisabled
	}

	mb := &Metricbeat{
		done:    done,
		modules: modules,
		config:  config,
		beat:    b,
		etcd:    etcd,
	}
	return mb, nil
}

// Run starts the workers for Metricbeat and blocks until Stop is called
// and the workers complete. Each host associated with a MetricSet is given its
// own goroutine for fetching data. The ensures that each host is isolated so
// that a single unresponsive host cannot inadvertently block other hosts
// within the same Module and MetricSet from collection.
func (bt *Metricbeat) Run(b *beat.Beat) error {
	var wg sync.WaitGroup

	for _, m := range bt.modules {

		client, err := m.connector.Connect()
		if err != nil {
			return err
		}

		r := module.NewRunner(client, m.module)
		r.Start()
		wg.Add(1)
		c, _ := bt.done[m.module.Name()]
		go func() {
			defer wg.Done()
			<-c
			r.Stop()
		}()
	}

	if bt.config.ConfigModules.Enabled() {
		moduleReloader := cfgfile.NewReloader(bt.config.ConfigModules)
		factory := module.NewFactory(bt.config.MaxStartDelay, b.Publisher)

		if err := moduleReloader.Check(factory); err != nil {
			return err
		}

		go moduleReloader.Run(factory)
		bt.reloadDone = make(chan struct{})
		wg.Add(1)
		go func() {
			defer wg.Done()
			<-bt.reloadDone
			moduleReloader.Stop()
		}()
	}

	dogChan := make(chan *clientv3.Event)

	go bt.etcd.WatchDog(dogChan)

	go moduleReload(dogChan, bt)

	wg.Wait()

	return nil
}

// Stop signals to Metricbeat that it should stop. It closes the "done" channel
// and closes the publisher client associated with each Module.
//
// Stop should only be called a single time. Calling it more than once may
// result in undefined behavior.
func (bt *Metricbeat) Stop() {
	for _, v := range bt.done {
		close(v)
	}
	close(bt.reloadDone)
}

// Modules return a list of all configured modules, including anyone present
// under dynamic config settings
func (bt *Metricbeat) Modules() ([]*module.Wrapper, error) {
	var modules []*module.Wrapper
	for _, m := range bt.modules {
		modules = append(modules, m.module)
	}

	// Add dynamic modules
	if bt.config.ConfigModules.Enabled() {
		config := cfgfile.DefaultDynamicConfig
		bt.config.ConfigModules.Unpack(&config)

		modulesManager, err := cfgfile.NewGlobManager(config.Path, ".yml", ".disabled")
		if err != nil {
			return nil, errors.Wrap(err, "initialization error")
		}

		for _, file := range modulesManager.ListEnabled() {
			confs, err := cfgfile.LoadList(file.Path)
			if err != nil {
				return nil, errors.Wrap(err, "error loading config files")
			}
			for _, conf := range confs {
				m, err := module.NewWrapper(time.Duration(0), conf, mb.Registry)
				if err != nil {
					return nil, errors.Wrap(err, "module initialization error")
				}
				modules = append(modules, m)
			}
		}
	}

	return modules, nil
}

func moduleReload(ch chan *clientv3.Event, bt *Metricbeat) error {

	var ev *clientv3.Event

	for {
		select {
		case ev = <-ch:
			logp.Info("%s %s : %s\n", ev.Type, ev.Kv.Key, ev.Kv.Value)
			switch ev.Type {

			case mvccpb.PUT:

				conf, err := FetchConfigsWithEtcd(bt.etcd)
				if err != nil {
					logp.Err("从ETCD获取配置异常:%s", err)
				} else {
					//停止原有模块
					d, _ := bt.done["jolokia"]
					close(d)
				}

				var errs multierror.Errors

				//根据新配置创建新模块
				for _, moduleCfg := range bt.config.Modules {

					if !moduleCfg.Enabled() {
						continue
					}
					fmt.Println(moduleCfg.GetFields())
					if k, _ := moduleCfg.String("module", -1); k == "jolokia" {
						if moduleCfg.HasField("hosts") && conf != nil {
							moduleCfg.SetChild("hosts", -1, conf)
						} else {
							continue
						}

					} else {
						continue
					}

					failed := false

					connector, err := module.NewConnector(bt.beat.Publisher, moduleCfg)
					if err != nil {
						errs = append(errs, err)
						failed = true
					}

					m, err := module.NewWrapper(bt.config.MaxStartDelay, moduleCfg, mb.Registry)
					if err != nil {
						errs = append(errs, err)
						failed = true
					}

					if failed {
						continue
					}

					for i, m := range bt.modules {
						if m.module.Name() == "jolokia" {
							bt.modules = append(bt.modules[:i], bt.modules[i+1:]...)
						}
					}

					bt.modules = append(bt.modules, staticModule{
						connector: connector,
						module:    m,
					})
					bt.done[m.Name()] = make(chan struct{})

					//启动新模块
					client, err := connector.Connect()
					if err != nil {
						return err
					}

					r := module.NewRunner(client, m)
					r.Start()
					go func() {
						c, _ := bt.done[m.Name()]
						<-c
						r.Stop()
					}()
				}
			//不处理delete事件
			default:

				return nil
			}
		}

	}
	return nil
}

// 从ETCD获取全量配置信息并覆盖配置文件配置
func FetchConfigsWithEtcd(etcdClient *etcd.EtcdClient) (config *common.Config, error *error) {

	conf, err := etcdClient.GetAllConfig()
	if err != nil {
		logp.Err("从ETCD获取metric配置异常:", err)
		return nil, err
	}
	var temp = make(map[string]bool)
	var c = []string{}
	for _, i := range conf {
		cf, err := i.Child("hosts", -1)
		if err == nil && cf.IsArray() {
			var v []string
			cf.Unpack(&v)
			for _, h := range v {
				if _, ok := temp[h]; !ok {
					c = append(c, h)
					temp[h] = true
				}
			}
		}
	}
	rs, e := common.NewConfigFrom(c)
	if e != nil {
		return rs, &e
	} else {
		return rs, nil
	}
}
