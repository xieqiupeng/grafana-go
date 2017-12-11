package system

import (
	"libbeat/logp"
	"metricbeat/helper"
)

func initModule() {
	if err := helper.CheckAndEnableSeDebugPrivilege(); err != nil {
		logp.Warn("%v", err)
	}
}
