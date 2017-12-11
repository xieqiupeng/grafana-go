package rocket

import (
	"errors"
	"rocketgo"
	"sync"
	"time"

	gometrics "github.com/rcrowley/go-metrics"

	"libbeat/beat"
	"libbeat/common"
	"libbeat/logp"
	"libbeat/outputs"
	"libbeat/outputs/codec"
	"libbeat/outputs/outil"
)

type rocket struct {

	config rocketConfig
	topic  outil.Selector
}

const (
	defaultWaitRetry = 1 * time.Second

	// NOTE: maxWaitRetry has no effect on mode, as logstash client currently does
	// not return ErrTempBulkFailure
	defaultMaxWaitRetry = 60 * time.Second
)

var rocketMetricsOnce sync.Once
var rocketMetricsRegistryInstance gometrics.Registry

var debugf = logp.MakeDebug("rocket")

var (
	errNoTopicSet = errors.New("No topic configured")
	errNoHosts    = errors.New("No hosts configured")
)



func init() {

	reg := gometrics.NewPrefixedRegistry("libbeat.rocket.")

	// Note: registers /debug/metrics handler for displaying all expvar counters
	// TODO: enable
	//exp.Exp(reg)
	rocketMetricsRegistryInstance = reg

	outputs.RegisterType("rocket", makeRocket)
}

func rocketMetricsRegistry() gometrics.Registry {
	return rocketMetricsRegistryInstance
}

func makeRocket(
	beat beat.Info,
	stats *outputs.Stats,
	cfg *common.Config,
) (outputs.Group, error) {
	debugf("initialize rocket output")

	config := defaultConfig
	if err := cfg.Unpack(&config); err != nil {
		return outputs.Fail(err)
	}

	topic, err := outil.BuildSelectorFromConfig(cfg, outil.Settings{
		Key:              "topic",
		MultiKey:         "topics",
		EnableSingleOnly: true,
		FailEmpty:        true,
	})
	if err != nil {
		return outputs.Fail(err)
	}

	libCfg, err := newRocketConfig(&config)
	if err != nil {
		return outputs.Fail(err)
	}

	codec, err := codec.CreateEncoder(beat, config.Codec)
	if err != nil {
		return outputs.Fail(err)
	}

	client, err := newRocketClient(stats, libCfg.Namesrv, beat.Beat, config.Key, topic, codec, libCfg)
	if err != nil {
		return outputs.Fail(err)
	}

	retry := 0
	if config.MaxRetries < 0 {
		retry = -1
	}
	return outputs.Success(config.BulkMaxSize, retry, client)
}

func newRocketConfig(config *rocketConfig) (*rocketmq.Config, error) {

	k := &rocketmq.Config{}

	k.Namesrv = config.NameSrv

	k.ClientIp = config.ClientIp

	k.InstanceName = config.InstanceName

	k.Topic = config.Topic

	k.ProducerGroup = config.ProducerGroup

	return k, nil
}
