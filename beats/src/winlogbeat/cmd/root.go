package cmd

import cmd "libbeat/cmd"
import "winlogbeat/beater"

// Name of this beat
var Name = "winlogbeat"

// RootCmd to handle beats cli
var RootCmd = cmd.GenRootCmd(Name, "", beater.New)
