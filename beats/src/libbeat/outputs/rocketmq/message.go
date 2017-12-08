package rocket

import (
	"rocketgo"
	"time"

	"libbeat/publisher"
)

type message struct {

	msg	  rocketmq.Message
	topic string
	key   []byte
	value []byte
	ref   *msgRef
	ts    time.Time

	hash      uint32
	partition int32

	data publisher.Event
}

var kafkaMessageKey interface{} = int(0)

func (m *message) initProducerMessage() {
	m.msg = rocketmq.Message{
		Topic: m.topic,
		Body: m.value,
		Properties: make(map[string]string),
	}
}
