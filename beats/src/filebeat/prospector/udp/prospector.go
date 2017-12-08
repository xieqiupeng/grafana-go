package udp

import (
	"filebeat/channel"
	"filebeat/harvester"
	"filebeat/prospector"
	"libbeat/common"
	"libbeat/common/cfgwarn"
	"libbeat/logp"
)

func init() {
	err := prospector.Register("udp", NewProspector)
	if err != nil {
		panic(err)
	}
}

type Prospector struct {
	harvester *Harvester
	started   bool
	outlet    channel.Outleter
}

func NewProspector(cfg *common.Config, outlet channel.Factory, context prospector.Context) (prospector.Prospectorer, error) {
	cfgwarn.Experimental("UDP prospector type is used")

	out, err := outlet(cfg)
	if err != nil {
		return nil, err
	}

	forwarder := harvester.NewForwarder(out)
	return &Prospector{
		outlet:    out,
		harvester: NewHarvester(forwarder, cfg),
		started:   false,
	}, nil
}

func (p *Prospector) Run() {
	logp.Info("Starting udp prospector")

	if !p.started {
		p.started = true
		go func() {
			defer p.outlet.Close()
			err := p.harvester.Run()
			if err != nil {
				logp.Err("Error running harvester:: %v", err)
			}
		}()
	}
}

func (p *Prospector) Stop() {
	logp.Info("Stopping udp prospector")
	p.harvester.Stop()
}

func (p *Prospector) Wait() {
	p.Stop()
}
