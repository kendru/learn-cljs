/*
Copyright © 2020 Andrew Meredith <andrew@learn-clojurescript.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package cmd

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
	"learn-cljs.com/notes/internal/note"
	"learn-cljs.com/notes/internal/transport"

	homedir "github.com/mitchellh/go-homedir"
	"github.com/spf13/viper"
)

var cfgFile string
var bindAddress string
var staticFileDir string

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
	Use:   "notes",
	Short: "RESTful backend for note-taking application",
	Run: func(cmd *cobra.Command, args []string) {
		notes := note.Service{note.NewInMemoryRepo()}
		httpServer := transport.NewHTTPServer(transport.Config{
			Addr:          bindAddress,
			StaticFileDir: staticFileDir,
		}, notes)
		httpServer.Serve()
	},
}

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func init() {
	cobra.OnInitialize(initConfig)

	// Here you will define your flags and configuration settings.
	// Cobra supports persistent flags, which, if defined here,
	// will be global for your application.

	rootCmd.PersistentFlags().StringVar(&cfgFile, "config", "", "config file (default is $HOME/.notes.yaml)")
	rootCmd.PersistentFlags().StringVar(&bindAddress, "addr", "0.0.0.0:8080", "address to which to bind server")
	rootCmd.PersistentFlags().StringVar(&staticFileDir, "dir", "./static", "Directory from which to serve static files")
}

// initConfig reads in config file and ENV variables if set.
func initConfig() {
	if cfgFile != "" {
		// Use config file from the flag.
		viper.SetConfigFile(cfgFile)
	} else {
		// Find home directory.
		home, err := homedir.Dir()
		if err != nil {
			fmt.Println(err)
			os.Exit(1)
		}

		// Search config in home directory with name ".notes" (without extension).
		viper.AddConfigPath(home)
		viper.SetConfigName(".notes")
	}
	if bindAddress == "" {
		bindAddress = "0.0.0.0:8080"
	}

	viper.AutomaticEnv() // read in environment variables that match

	// If a config file is found, read it in.
	if err := viper.ReadInConfig(); err == nil {
		fmt.Println("Using config file:", viper.ConfigFileUsed())
	}
}
