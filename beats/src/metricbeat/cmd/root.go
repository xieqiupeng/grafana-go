package cmd

import (
	"flag"

	"github.com/spf13/pflag"

	cmd "libbeat/cmd"
	"metricbeat/beater"
	"metricbeat/cmd/test"

	// import modules
	_ "metricbeat/include"
)

// Name of this beat
var Name = "metricbeat"

// RootCmd to handle beats cli
var RootCmd *cmd.BeatsRootCmd

func init() {
	var runFlags = pflag.NewFlagSet(Name, pflag.ExitOnError)
	runFlags.AddGoFlag(flag.CommandLine.Lookup("system.hostfs"))

	RootCmd = cmd.GenRootCmdWithRunFlags(Name, "", beater.New, runFlags)
	RootCmd.AddCommand(cmd.GenModulesCmd(Name, "", buildModulesManager))
	RootCmd.TestCmd.AddCommand(test.GenTestModulesCmd(Name, ""))
}
