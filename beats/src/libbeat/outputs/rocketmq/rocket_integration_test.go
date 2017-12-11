// +build integration

package rocket

import (
	"encoding/json"
	"fmt"
	"os"
	"sync"
	"testing"
	"time"
	"math/rand"
	"strconv"
	"github.com/stretchr/testify/assert"

	"libbeat/beat"
	"libbeat/common"
	"libbeat/common/fmtstr"
	"libbeat/logp"
	"libbeat/outputs/outest"

	_ "libbeat/outputs/codec/format"
	_ "libbeat/outputs/codec/json"
)

const (
	rocketNameSrv = "10.100.139.149:9876"
)

type eventInfo struct {
	events []beat.Event
}

func TestRocketPublish(t *testing.T) {
	if testing.Verbose() {
		logp.LogInit(logp.LOG_DEBUG, "", false, true, []string{"rocket"})
	}

	id := strconv.Itoa(rand.New(rand.NewSource(int64(time.Now().Nanosecond()))).Int())

	testTopic := "TopicTest"
	logType := "TopicTest"

	tests := []struct {
		title  string
		config map[string]interface{}
		topic  string
		events []eventInfo
	}{
		{
			"publish single event to test topic",
			nil,
			testTopic,
			single(common.MapStr{
				"host":    "test-host",
				"message": id,
			}),
		},
		{
			"publish single event with topic from type",
			map[string]interface{}{
				"topic": "%{[type]}",
			},
			logType,
			single(common.MapStr{
				"host":    "test-host",
				"type":    logType,
				"message": id,
			}),
		},
		{
			"publish single event with formating to test topic",
			map[string]interface{}{
				"codec.format.string": "%{[message]}",
			},
			testTopic,
			single(common.MapStr{
				"host":    "test-host",
				"message": id,
			}),
		},
		{
			"batch publish to test topic",
			nil,
			testTopic,
			randMulti(5, 100, common.MapStr{
				"host": "test-host",
			}),
		},
		{
			"batch publish to test topic from type",
			map[string]interface{}{
				"topic": "%{[type]}",
			},
			logType,
			randMulti(5, 100, common.MapStr{
				"host": "test-host",
				"type": logType,
			}),
		},
		{
			"batch publish with random partitioner",
			map[string]interface{}{
				"partition.random": map[string]interface{}{
					"group_events": 1,
				},
			},
			testTopic,
			randMulti(1, 10, common.MapStr{
				"host": "test-host",
				"type": "log",
			}),
		},
		{
			"batch publish with round robin partitioner",
			map[string]interface{}{
				"partition.round_robin": map[string]interface{}{
					"group_events": 1,
				},
			},
			testTopic,
			randMulti(1, 10, common.MapStr{
				"host": "test-host",
				"type": "log",
			}),
		},
		{
			"batch publish with hash partitioner without key (fallback to random)",
			map[string]interface{}{
				"partition.hash": map[string]interface{}{},
			},
			testTopic,
			randMulti(1, 10, common.MapStr{
				"host": "test-host",
				"type": "log",
			}),
		},
		{
			// warning: this test uses random keys. In case keys are reused, test might fail.
			"batch publish with hash partitioner with key",
			map[string]interface{}{
				"key":            "%{[message]}",
				"partition.hash": map[string]interface{}{},
			},
			testTopic,
			randMulti(1, 10, common.MapStr{
				"host": "test-host",
				"type": "log",
			}),
		},
		{
			// warning: this test uses random keys. In case keys are reused, test might fail.
			"batch publish with fields hash partitioner",
			map[string]interface{}{
				"partition.hash.hash": []string{
					"@timestamp",
					"type",
					"message",
				},
			},
			testTopic,
			randMulti(1, 10, common.MapStr{
				"host": "test-host",
				"type": "log",
			}),
		},
	}

	defaultConfig := map[string]interface{}{
		"namesrv":   rocketNameSrv,
		"topic"	: "TopicTest",
		"producergroup": "ProducerGroupName",
		"clientip": "10.10.180.66",
		"instancename": "DEFAULT",
	}


	cfg := makeConfig(t, defaultConfig)
	grp, err := makeRocket(beat.Info{Beat: "libbeat"}, nil, cfg)
	if err != nil {
		t.Fatal(err)
	}

	output := grp.Clients[0].(*client)
	if err := output.Connect(); err != nil {
		t.Fatal(err)
	}
	defer output.Close()

	for i, test := range tests {
		test := test
		name := fmt.Sprintf("run test(%v): %v", i, test.title)

		t.Run(name, func(t *testing.T) {

			// publish test events
			var wg sync.WaitGroup
			for i := range test.events {
				batch := outest.NewBatch(test.events[i].events...)
				batch.OnSignal = func(_ outest.BatchSignal) {
					wg.Done()
				}

				wg.Add(1)
				output.Publish(batch)
			}

			// wait for all published batches to be ACKed
			wg.Wait()

		})
	}
}

func validateJSON(t *testing.T, value []byte, event beat.Event) {
	var decoded map[string]interface{}
	err := json.Unmarshal(value, &decoded)
	if err != nil {
		t.Errorf("can not json decode event value: %v", value)
		return
	}
	assert.Equal(t, decoded["type"], event.Fields["type"])
	assert.Equal(t, decoded["message"], event.Fields["message"])
}

func makeValidateFmtStr(fmt string) func(*testing.T, []byte, beat.Event) {
	fmtString := fmtstr.MustCompileEvent(fmt)
	return func(t *testing.T, value []byte, event beat.Event) {
		expectedMessage, err := fmtString.Run(&event)
		if err != nil {
			t.Fatal(err)
		}
		assert.Equal(t, string(expectedMessage), string(value))
	}
}

func strDefault(a, defaults string) string {
	if len(a) == 0 {
		return defaults
	}
	return a
}

func getenv(name, defaultValue string) string {
	return strDefault(os.Getenv(name), defaultValue)
}


func makeConfig(t *testing.T, in map[string]interface{}) *common.Config {
	cfg, err := common.NewConfigFrom(in)
	if err != nil {
		t.Fatal(err)
	}
	return cfg
}



var testTopicOffsets = map[string]int64{}



func flatten(infos []eventInfo) []beat.Event {
	var out []beat.Event
	for _, info := range infos {
		out = append(out, info.events...)
	}
	return out
}

func single(fields common.MapStr) []eventInfo {
	return []eventInfo{
		{
			events: []beat.Event{
				{Timestamp: time.Now(), Fields: fields},
			},
		},
	}
}

func randMulti(batches, n int, event common.MapStr) []eventInfo {
	var out []eventInfo
	for i := 0; i < batches; i++ {
		var data []beat.Event
		for j := 0; j < n; j++ {
			tmp := common.MapStr{}
			for k, v := range event {
				tmp[k] = v
			}
			tmp["message"] = randString(100)
			data = append(data, beat.Event{Timestamp: time.Now(), Fields: tmp})
		}

		out = append(out, eventInfo{data})
	}
	return out
}
