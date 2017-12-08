package logstash

import (
	"libbeat/beat"
	"libbeat/outputs/codec/json"
)

func makeLogstashEventEncoder(info beat.Info, index string) func(interface{}) ([]byte, error) {
	enc := json.New(false, info.Version)
	return func(event interface{}) ([]byte, error) {
		return enc.Encode(index, event.(*beat.Event))
	}
}
