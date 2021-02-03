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
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/spf13/cobra"
	"learn-cljs.com/notes/internal/note"
	"learn-cljs.com/notes/internal/transport"

	homedir "github.com/mitchellh/go-homedir"
	"github.com/spf13/viper"
)

var cfgFile string

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
	Use:   "notes",
	Short: "RESTful backend for note-taking application",
	Run: func(cmd *cobra.Command, args []string) {
		var cfg Config
		if err := viper.Unmarshal(&cfg); err != nil {
			log.Fatalf("error unmarshaling config: %v", err)
		}

		if cfg.SigningSecret == "" {
			log.Fatalf("signing secret must be set")
		}

		idx, err := note.NewBleveSearchindex(cfg.Search)
		if err != nil {
			log.Fatalf("error creating repository: %v", err)
		}

		repository, err := note.NewRepository(cfg.Repository, idx)
		if err != nil {
			log.Fatalf("error creating repository: %v", err)
		}

		ctx, cancel := context.WithCancel(context.Background())

		service := note.NewService(repository, idx)
		server := transport.NewHTTPServer(
			transport.Config{
				Addr:          cfg.BindAddress,
				StaticFileDir: cfg.StaticFileDir,
				Context:       ctx,
				NoteService:   service,
				SigningSecret: []byte(cfg.SigningSecret),
			},
		)

		go func() {
			if err := server.Serve(); err != http.ErrServerClosed {
				fmt.Printf("error in HTTP server: %v", err)
			}
		}()

		signalChan := make(chan os.Signal, 1)
		signal.Notify(
			signalChan,
			syscall.SIGHUP,
			syscall.SIGINT,
			syscall.SIGQUIT,
		)

		<-signalChan
		log.Println("Received interrupt. Stopping gracefully. Ctl+C will force stop.")

		go func() {
			<-signalChan
			log.Fatalln("Received abort signal. Terminating.")
		}()

		gracefullCtx, cancelShutdown := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancelShutdown()

		if err := server.Shutdown(gracefullCtx); err != nil {
			log.Printf("shutdown error: %v\n", err)
			defer os.Exit(1)
		}
		service.Close()

		// manually cancel context if not using httpServer.RegisterOnShutdown(cancel)
		cancel()

		defer os.Exit(0)
	},
}

type Config struct {
	BindAddress   string `mapstructure:"addr"`
	StaticFileDir string `mapstructure:"dir"`
	SigningSecret string `mapstructure:"signing-secret"`
	Repository    note.RepositoryConfig
	Search        note.SearchIndexConfig
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

	rootCmd.PersistentFlags().StringVar(&cfgFile, "config", "", "config file (default is $HOME/.notes.yaml)")
	rootCmd.PersistentFlags().String("addr", "0.0.0.0:8080", "address to which to bind server")
	rootCmd.PersistentFlags().String("dir", "./static", "Directory from which to serve static files")
	rootCmd.PersistentFlags().String("signing-secret", "", "Secret used to sign tenantIDs")

	rootCmd.PersistentFlags().String("repository.type", "memory", "repo type")
	rootCmd.PersistentFlags().String("repository.badger-dir", "./db-data/kv", "Badger repository directory")

	rootCmd.PersistentFlags().String("search.bleve-path", "./db-data/search/index.bleve", "Search index file")
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

	viper.AutomaticEnv() // read in environment variables that match
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	viper.SetEnvPrefix("STS")

	if err := viper.BindPFlags(rootCmd.PersistentFlags()); err != nil {
		panic(fmt.Sprintf("Could not bind config: %v", err))
	}

	// If a config file is found, read it in.
	if err := viper.ReadInConfig(); err == nil {
		fmt.Println("Using config file:", viper.ConfigFileUsed())
	}
}
