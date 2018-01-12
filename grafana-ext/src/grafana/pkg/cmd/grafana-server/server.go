package main

import (
	"context"
	"flag"
	"io/ioutil"
	"os"
	"path/filepath"
	"strconv"
	"time"

	"grafana/pkg/cmd/grafana-cli/logger"
	"grafana/pkg/services/provisioning"

	"golang.org/x/sync/errgroup"

	"grafana/pkg/api"
	"grafana/pkg/log"
	"grafana/pkg/login"
	"grafana/pkg/metrics"
	"grafana/pkg/models"
	"grafana/pkg/plugins"
	"grafana/pkg/services/alerting"
	"grafana/pkg/services/cleanup"
	"grafana/pkg/services/notifications"
	"grafana/pkg/services/search"
	"grafana/pkg/services/sqlstore"
	"grafana/pkg/setting"

	"grafana/pkg/social"
	"grafana/pkg/tracing"
)

func NewGrafanaServer() models.GrafanaServer {
	rootCtx, shutdownFn := context.WithCancel(context.Background())
	childRoutines, childCtx := errgroup.WithContext(rootCtx)

	return &GrafanaServerImpl{
		context:       childCtx,
		shutdownFn:    shutdownFn,
		childRoutines: childRoutines,
		log:           log.New("server"),
	}
}

type GrafanaServerImpl struct {
	context       context.Context
	shutdownFn    context.CancelFunc
	childRoutines *errgroup.Group
	log           log.Logger

	httpServer *api.HttpServer
}

func (g *GrafanaServerImpl) Start() {
	go listenToSystemSignals(g)

	g.initLogging()
	g.writePIDFile()

	initSql()

	metrics.Init(setting.Cfg)
	search.Init()
	login.Init()
	social.NewOAuthService()
	plugins.Init()

	if err := provisioning.StartUp(setting.DatasourcesPath); err != nil {
		logger.Error("Failed to provision Grafana from config", "error", err)
		g.Shutdown(1, "Startup failed")
		return
	}

	closer, err := tracing.Init(setting.Cfg)
	if err != nil {
		g.log.Error("Tracing settings is not valid", "error", err)
		g.Shutdown(1, "Startup failed")
		return
	}
	defer closer.Close()

	// init alerting
	if setting.AlertingEnabled && setting.ExecuteAlerts {
		engine := alerting.NewEngine()
		g.childRoutines.Go(func() error { return engine.Run(g.context) })
	}

	// cleanup service
	cleanUpService := cleanup.NewCleanUpService()
	g.childRoutines.Go(func() error { return cleanUpService.Run(g.context) })

	if err = notifications.Init(); err != nil {
		g.log.Error("Notification service failed to initialize", "error", err)
		g.Shutdown(1, "Startup failed")
		return
	}

	g.startHttpServer()
}

func initSql() {
	sqlstore.NewEngine()
	sqlstore.EnsureAdminUser()
}

func (g *GrafanaServerImpl) initLogging() {
	err := setting.NewConfigContext(&setting.CommandLineArgs{
		Config:   *configFile,
		HomePath: *homePath,
		Args:     flag.Args(),
	})

	if err != nil {
		g.log.Error(err.Error())
		os.Exit(1)
	}

	g.log.Info("Starting Grafana", "version", version, "commit", commit, "compiled", time.Unix(setting.BuildStamp, 0))
	setting.LogConfigurationInfo()
}

func (g *GrafanaServerImpl) startHttpServer() {
	g.httpServer = api.NewHttpServer()

	err := g.httpServer.Start(g.context)

	if err != nil {
		g.log.Error("Fail to start server", "error", err)
		g.Shutdown(1, "Startup failed")
		return
	}
}

func (g *GrafanaServerImpl) Shutdown(code int, reason string) {
	g.log.Info("Shutdown started", "code", code, "reason", reason)

	err := g.httpServer.Shutdown(g.context)
	if err != nil {
		g.log.Error("Failed to shutdown server", "error", err)
	}

	g.shutdownFn()
	err = g.childRoutines.Wait()

	g.log.Info("Shutdown completed", "reason", err)
	log.Close()
	os.Exit(code)
}

func (g *GrafanaServerImpl) writePIDFile() {
	if *pidFile == "" {
		return
	}

	// Ensure the required directory structure exists.
	err := os.MkdirAll(filepath.Dir(*pidFile), 0700)
	if err != nil {
		g.log.Error("Failed to verify pid directory", "error", err)
		os.Exit(1)
	}

	// Retrieve the PID and write it.
	pid := strconv.Itoa(os.Getpid())
	if err := ioutil.WriteFile(*pidFile, []byte(pid), 0644); err != nil {
		g.log.Error("Failed to write pidfile", "error", err)
		os.Exit(1)
	}

	g.log.Info("Writing PID file", "path", *pidFile, "pid", pid)
}
