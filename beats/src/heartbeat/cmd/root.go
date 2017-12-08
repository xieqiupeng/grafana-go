package cmd

import (
	// register default heartbeat monitors
	_ "heartbeat/monitors/defaults"

	"heartbeat/beater"
	cmd "libbeat/cmd"
)

// Name of this beat
var Name = "heartbeat"

// RootCmd to handle beats cli
var RootCmd = cmd.GenRootCmd(Name, "", beater.New)
