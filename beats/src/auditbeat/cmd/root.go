package cmd

import (
	"github.com/spf13/pflag"

	"metricbeat/beater"

	cmd "libbeat/cmd"
)

// Name of the beat (auditbeat).
const Name = "auditbeat"

// RootCmd for running auditbeat.
var RootCmd *cmd.BeatsRootCmd

func init() {
	var runFlags = pflag.NewFlagSet(Name, pflag.ExitOnError)
	RootCmd = cmd.GenRootCmdWithRunFlags(Name, "", beater.New, runFlags)
}
