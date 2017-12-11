package channel

import (
	"filebeat/util"
	"libbeat/common"
)

// Factory is used to create a new Outlet instance
type Factory func(*common.Config) (Outleter, error)

// Outleter is the outlet for a prospector
type Outleter interface {
	Close() error
	OnEvent(data *util.Data) bool
}
