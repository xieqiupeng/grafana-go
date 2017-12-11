package rocket

import (
	"errors"
	"time"

	"libbeat/common"
	"libbeat/common/fmtstr"
	"libbeat/outputs"
	"libbeat/outputs/codec"
)

type rocketConfig struct {
	NameSrv         string                    `config:"namesrv"               validate:"required"`
	ProducerGroup   string					  `config:"producergroup" validate:"required"`
	Topic			string					  `config:"topic"			validate:"required"`
	ClientIp		string					  `config:"clientip"`
	InstanceName	string					  `config:"instancename"`
	TLS             *outputs.TLSConfig        `config:"ssl"`
	Timeout         time.Duration             `config:"timeout"             validate:"min=1"`
	Metadata        metaConfig                `config:"metadata"`
	Key             *fmtstr.EventFormatString `config:"key"`
	Partition       map[string]*common.Config `config:"partition"`
	KeepAlive       time.Duration             `config:"keep_alive"          validate:"min=0"`
	MaxMessageBytes *int                      `config:"max_message_bytes"   validate:"min=1"`
	RequiredACKs    *int                      `config:"required_acks"       validate:"min=-1"`
	BrokerTimeout   time.Duration             `config:"broker_timeout"      validate:"min=1"`
	Compression     string                    `config:"compression"`
	Version         string                    `config:"version"`
	BulkMaxSize     int                       `config:"bulk_max_size"`
	MaxRetries      int                       `config:"max_retries"         validate:"min=-1,nonzero"`
	ClientID        string                    `config:"client_id"`
	ChanBufferSize  int                       `config:"channel_buffer_size" validate:"min=1"`
	Username        string                    `config:"username"`
	Password        string                    `config:"password"`
	Codec           codec.Config              `config:"codec"`
}

type metaConfig struct {
	Retry       metaRetryConfig `config:"retry"`
	RefreshFreq time.Duration   `config:"refresh_frequency" validate:"min=0"`
}

type metaRetryConfig struct {
	Max     int           `config:"max"     validate:"min=0"`
	Backoff time.Duration `config:"backoff" validate:"min=0"`
}

var (
	defaultConfig = rocketConfig{
		NameSrv:       "",
		ProducerGroup: "",
		ClientIp: "",
		Topic:		   "",
		InstanceName:  "",
		TLS:         nil,
		Timeout:     30 * time.Second,
		BulkMaxSize: 2048,
		Metadata: metaConfig{
			Retry: metaRetryConfig{
				Max:     3,
				Backoff: 250 * time.Millisecond,
			},
			RefreshFreq: 10 * time.Minute,
		},
		KeepAlive:       0,
		MaxMessageBytes: nil, // use library default
		RequiredACKs:    nil, // use library default
		BrokerTimeout:   10 * time.Second,
		Compression:     "gzip",
		Version:         "",
		MaxRetries:      3,
		ClientID:        "beats",
		ChanBufferSize:  256,
		Username:        "",
		Password:        "",
	}
)

func (c *rocketConfig) Validate() error {
	if len(c.NameSrv) == 0 {
		return errors.New("no namesrv configured")
	}
	return nil
}
