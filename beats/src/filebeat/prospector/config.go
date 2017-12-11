package prospector

import (
	"time"

	cfg "filebeat/config"
	"libbeat/common/cfgwarn"
)

var (
	defaultConfig = prospectorConfig{
		ScanFrequency: 10 * time.Second,
		Type:          cfg.DefaultType,
	}
)

type prospectorConfig struct {
	ScanFrequency time.Duration `config:"scan_frequency" validate:"min=0,nonzero"`
	Type          string        `config:"type"`
	InputType     string        `config:"input_type"`
}

func (c *prospectorConfig) Validate() error {
	if c.InputType != "" {
		cfgwarn.Deprecate("6.0.0", "input_type prospector config is deprecated. Use type instead.")
		c.Type = c.InputType
	}
	return nil
}