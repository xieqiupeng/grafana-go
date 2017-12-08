package memcache

import (
	"time"

	"packetbeat/config"
	"packetbeat/protos"
)

type memcacheConfig struct {
	config.ProtocolCommon `config:",inline"`
	MaxValues             int
	MaxBytesPerValue      int
	UDPTransactionTimeout time.Duration
	ParseUnknown          bool
}

var (
	defaultConfig = memcacheConfig{
		ProtocolCommon: config.ProtocolCommon{
			TransactionTimeout: protos.DefaultTransactionExpiration,
		},
		UDPTransactionTimeout: protos.DefaultTransactionExpiration,
	}
)
