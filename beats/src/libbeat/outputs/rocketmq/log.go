package rocket

import (
	"github.com/Shopify/sarama"

	"libbeat/logp"
)

type RocketLogger struct{}

func (rl RocketLogger) Print(v ...interface{}) {
	rl.Log("rocketmq message: %v", v)
}

func (rl RocketLogger) Printf(format string, v ...interface{}) {
	rl.Log(format, v)
}

func (rl RocketLogger) Println(v ...interface{}) {
	rl.Log("rocketmq message: %v", v...)
}

func (RocketLogger) Log(format string, v ...interface{}) {
	warn := false
	for _, val := range v {
		if err, ok := val.(sarama.KError); ok {
			if err != sarama.ErrNoError {
				warn = true
				break
			}
		}
	}
	if warn {
		logp.Warn(format, v)
	} else {
		logp.Info(format, v)
	}
}
