package main

import (
	"os"

	"auditbeat/cmd"

	_ "auditbeat/module/audit"
	_ "auditbeat/module/audit/file"
	_ "auditbeat/module/audit/kernel"
)

func main() {
	if err := cmd.RootCmd.Execute(); err != nil {
		os.Exit(1)
	}
}
