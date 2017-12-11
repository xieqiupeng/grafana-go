package cmd

import (
	"flag"

	"github.com/spf13/pflag"

	"filebeat/beater"

	cmd "libbeat/cmd"
)

// Name of this beat
var Name = "filebeat"

// RootCmd to handle beats cli
var RootCmd *cmd.BeatsRootCmd

func init() {
	var runFlags = pflag.NewFlagSet(Name, pflag.ExitOnError)
	runFlags.AddGoFlag(flag.CommandLine.Lookup("once"))
	runFlags.AddGoFlag(flag.CommandLine.Lookup("modules"))

	RootCmd = cmd.GenRootCmdWithRunFlags(Name, "", beater.New, runFlags)
	RootCmd.PersistentFlags().AddGoFlag(flag.CommandLine.Lookup("M"))
	RootCmd.TestCmd.Flags().AddGoFlag(flag.CommandLine.Lookup("modules"))
	RootCmd.SetupCmd.Flags().AddGoFlag(flag.CommandLine.Lookup("modules"))
	RootCmd.AddCommand(cmd.GenModulesCmd(Name, "", buildModulesManager))
}
