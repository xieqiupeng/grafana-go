package crawler

import (
	"fmt"
	"sync"

	"filebeat/channel"
	"filebeat/fileset"
	"filebeat/input/file"
	"filebeat/prospector"
	"filebeat/registrar"
	"libbeat/cfgfile"
	"libbeat/common"
	"libbeat/logp"

	_ "filebeat/include"
)

type Crawler struct {
	prospectors         map[uint64]*prospector.Prospector
	prospectorConfigs   []*common.Config
	out                 channel.Factory
	wg                  sync.WaitGroup
	modulesReloader     *cfgfile.Reloader
	prospectorsReloader *cfgfile.Reloader
	once                bool
	beatVersion         string
	beatDone            chan struct{}
}

func New(out channel.Factory, prospectorConfigs []*common.Config, beatVersion string, beatDone chan struct{}, once bool) (*Crawler, error) {
	return &Crawler{
		out:               out,
		prospectors:       map[uint64]*prospector.Prospector{},
		prospectorConfigs: prospectorConfigs,
		once:              once,
		beatVersion:       beatVersion,
		beatDone:          beatDone,
	}, nil
}

// Start starts the crawler with all prospectors
func (c *Crawler) Start(r *registrar.Registrar, configProspectors *common.Config,
	configModules *common.Config, pipelineLoaderFactory fileset.PipelineLoaderFactory) error {

	logp.Info("Loading Prospectors: %v", len(c.prospectorConfigs))

	// Prospect the globs/paths given on the command line and launch harvesters
	for _, prospectorConfig := range c.prospectorConfigs {
		err := c.startProspector(prospectorConfig, r.GetStates())
		if err != nil {
			return err
		}
	}

	if configProspectors.Enabled() {
		c.prospectorsReloader = cfgfile.NewReloader(configProspectors)
		runnerFactory := prospector.NewRunnerFactory(c.out, r, c.beatDone)
		if err := c.prospectorsReloader.Check(runnerFactory); err != nil {
			return err
		}

		go func() {
			c.prospectorsReloader.Run(runnerFactory)
		}()
	}

	if configModules.Enabled() {
		c.modulesReloader = cfgfile.NewReloader(configModules)
		modulesFactory := fileset.NewFactory(c.out, r, c.beatVersion, pipelineLoaderFactory, c.beatDone)
		if err := c.modulesReloader.Check(modulesFactory); err != nil {
			return err
		}

		go func() {
			c.modulesReloader.Run(modulesFactory)
		}()
	}

	logp.Info("Loading and starting Prospectors completed. Enabled prospectors: %v", len(c.prospectors))

	return nil
}

func (c *Crawler) startProspector(config *common.Config, states []file.State) error {
	if !config.Enabled() {
		return nil
	}
	p, err := prospector.New(config, c.out, c.beatDone, states)
	if err != nil {
		return fmt.Errorf("Error in initing prospector: %s", err)
	}
	p.Once = c.once

	if _, ok := c.prospectors[p.ID]; ok {
		return fmt.Errorf("Prospector with same ID already exists: %d", p.ID)
	}

	c.prospectors[p.ID] = p

	p.Start()

	return nil
}

func (c *Crawler) Stop() {
	logp.Info("Stopping Crawler")

	asyncWaitStop := func(stop func()) {
		c.wg.Add(1)
		go func() {
			defer c.wg.Done()
			stop()
		}()
	}

	logp.Info("Stopping %v prospectors", len(c.prospectors))
	for _, p := range c.prospectors {
		// Stop prospectors in parallel
		asyncWaitStop(p.Stop)
	}

	if c.prospectorsReloader != nil {
		asyncWaitStop(c.prospectorsReloader.Stop)
	}

	if c.modulesReloader != nil {
		asyncWaitStop(c.modulesReloader.Stop)
	}

	c.WaitForCompletion()

	logp.Info("Crawler stopped")
}

func (c *Crawler) WaitForCompletion() {
	c.wg.Wait()
}
