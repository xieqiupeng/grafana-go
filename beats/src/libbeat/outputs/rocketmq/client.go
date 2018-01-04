package rocket

import (
	"fmt"
	"sync"
	"sync/atomic"

	"libbeat/common/fmtstr"
	"libbeat/logp"
	"libbeat/outputs"
	"libbeat/outputs/codec"
	"libbeat/outputs/outil"
	"libbeat/publisher"
	"rocketgo"
)

type client struct {
	stats    *outputs.Stats
	hosts    string
	topic    outil.Selector
	key      *fmtstr.EventFormatString
	index    string
	codec    codec.Codec
	config   rocketmq.Config
	group    string
	producer rocketmq.Producer

	wg sync.WaitGroup
}

type msgRef struct {
	client *client
	count  int32
	total  int
	failed []publisher.Event
	batch  publisher.Batch

	err error
}

func newRocketClient(
	stats *outputs.Stats,
	hosts string,
	index string,
	key *fmtstr.EventFormatString,
	topic outil.Selector,
	writer codec.Codec,
	cfg *rocketmq.Config,
) (*client, error) {
	c := &client{
		stats:  stats,
		hosts:  hosts,
		topic:  topic,
		key:    key,
		index:  index,
		codec:  writer,
		config: *cfg,
		group:  cfg.ProducerGroup,
	}
	return c, nil
}

func (c *client) Connect() error {
	debugf("connect: %v", c.hosts)

	// try to connect
	producer, err := rocketmq.NewDefaultProducer(c.group, &c.config)
	if err != nil {
		logp.Err("Kafka connect fails with: %v", err)
		return err
	}

	c.producer = producer
	c.producer.Start()
	c.wg.Add(1)
	//go c.successWorker(producer.Successes())
	//go c.errorWorker(producer.Errors())

	return nil
}

func (c *client) Close() error {
	debugf("closed kafka client")

	c.producer.Shutdown()
	c.wg.Wait()
	c.producer = nil
	return nil
}

func (c *client) Publish(batch publisher.Batch) error {
	events := batch.Events()
	c.stats.NewBatch(len(events))

	ref := &msgRef{
		client: c,
		count:  int32(len(events)),
		total:  len(events),
		failed: nil,
		batch:  batch,
	}

	for i := range events {
		d := &events[i]
		msg, err := c.getEventMessage(d)
		if err != nil {
			logp.Err("Dropping event: %v", err)
			ref.done()
			c.stats.Dropped(1)
			continue
		}

		msg.ref = ref
		msg.initProducerMessage()

		sendCallback := func(result *rocketmq.SendResult, err2 error) error {
			if err2 != nil {
				logp.Err("err->%s", err)
				return err2
			} else {
				id, status := result.GetInfo()
				logp.Info("%s:%d", id, status)
			}

			msg.ref.done()
			return nil
		}
		logp.Debug("bingo", "start sendasync ...")
		error := c.producer.SendAsync(&msg.msg, sendCallback)
		logp.Debug("bingo", "end sendasync ...")
		if error != nil {
			logp.Err("sendAsync:%s", error)
		} else {
			logp.Info("sendAsync success!")
		}

		//
		//if sendResult, err := c.producer.Send(&msg.msg); err != nil {
		//	logp.Err("Sync send fail!") // 如果不是如预期的那么就报错
		//	logp.Err("err->%s", err)
		//	msg.ref.done()
		//} else {
		//	logp.Info("sendResult", sendResult)
		//	logp.Info("Sync send success, %d", i)
		//	msg.ref.done()
		//}
	}

	return nil
}

func (c *client) getEventMessage(data *publisher.Event) (*message, error) {
	event := &data.Content
	msg := &message{partition: -1, data: *data}
	if event.Meta != nil {
		if value, ok := event.Meta["partition"]; ok {
			if partition, ok := value.(int32); ok {
				msg.partition = partition
			}
		}

		if value, ok := event.Meta["topic"]; ok {
			if topic, ok := value.(string); ok {
				msg.topic = topic
			}
		}
	}
	if msg.topic == "" {
		topic, err := c.topic.Select(event)
		if err != nil {
			return nil, fmt.Errorf("setting rocket topic failed with %v", err)
		}
		msg.topic = topic
		if event.Meta == nil {
			event.Meta = map[string]interface{}{}
		}
		event.Meta["topic"] = topic
	}

	serializedEvent, err := c.codec.Encode(c.index, event)
	if err != nil {
		return nil, err
	}

	buf := make([]byte, len(serializedEvent))
	copy(buf, serializedEvent)
	msg.value = buf

	if c.key != nil {
		if key, err := c.key.RunBytes(event); err == nil {
			msg.key = key
		}
	}

	return msg, nil
}

/*
func (c *client) successWorker(ch <-chan *sarama.ProducerMessage) {
	defer c.wg.Done()
	defer debugf("Stop kafka ack worker")

	for libMsg := range ch {
		msg := libMsg.Metadata.(*message)
		msg.ref.done()
	}
}

func (c *client) errorWorker(ch <-chan *sarama.ProducerError) {
	defer c.wg.Done()
	defer debugf("Stop kafka error handler")

	for errMsg := range ch {
		msg := errMsg.Msg.Metadata.(*message)
		msg.ref.fail(msg, errMsg.Err)
	}
}
*/
func (r *msgRef) done() {
	r.dec()
}

func (r *msgRef) fail(msg *message, err error) {

	r.failed = append(r.failed, msg.data)
	r.err = err
	r.dec()
}

func (r *msgRef) dec() {
	i := atomic.AddInt32(&r.count, -1)
	if i > 0 {
		return
	}

	debugf("finished rocket batch")
	stats := r.client.stats

	err := r.err
	if err != nil {
		failed := len(r.failed)
		success := r.total - failed
		r.batch.RetryEvents(r.failed)

		stats.Failed(failed)
		if success > 0 {
			stats.Acked(success)
		}

		debugf("rocket publish failed with: %v", err)
	} else {
		r.batch.ACK()
		stats.Acked(r.total)
	}
}
