// Copyright © 2017 NAME HERE <EMAIL ADDRESS>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package cmd

import (
	"libbeat/logp"
	"manager/ctr"
	"time"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

// runCmdCmd represents the runCmd command
var runCmd = &cobra.Command{
	Use:   "run",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {

		var mds []map[string]string
		err := viper.UnmarshalKey("modules", &mds)
		if err != nil || len(mds) == 0 {
			panic("no beat module config !!!")
		}
		endpoints := viper.GetStringSlice("etcd.config.endpoints")
		if endpoints == nil || len(endpoints) == 0 {
			panic("etcd.config.endpoints must have value!")
		}
		dialtimeout := viper.GetDuration("etcd.config.dialtimeout")
		if dialtimeout == 0 {
			dialtimeout = time.Duration(5)
		}
		username := viper.GetString("etcd.config.username")

		password := viper.GetString("etcd.config.password")

		key := viper.GetString("etcd.configpath")

		if key == "" {
			key = "/monitor/"
		}

		bt, error := ctr.New(endpoints, dialtimeout, username, password, key, mds)
		if error == nil {
			err := bt.Run()
			if err != nil {
				logp.Err("启动失败:%v", err)
			}
		}
	},
}

func init() {
	rootCmd.AddCommand(runCmd)

	// Here you will define your flags and configuration settings.

	// Cobra supports Persistent Flags which will work for this command
	// and all subcommands, e.g.:
	// runCmdCmd.PersistentFlags().String("foo", "", "A help for foo")

	// Cobra supports local flags which will only run when this command
	// is called directly, e.g.:
	// runCmdCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
}
