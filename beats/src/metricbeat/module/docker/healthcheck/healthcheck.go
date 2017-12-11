package healthcheck

import (
	dc "github.com/fsouza/go-dockerclient"

	"libbeat/common"
	"libbeat/common/cfgwarn"
	"metricbeat/mb"
	"metricbeat/module/docker"
)

func init() {
	if err := mb.Registry.AddMetricSet("docker", "healthcheck", New, docker.HostParser); err != nil {
		panic(err)
	}
}

type MetricSet struct {
	mb.BaseMetricSet
	dockerClient *dc.Client
}

// New creates a new instance of the docker healthcheck MetricSet.
func New(base mb.BaseMetricSet) (mb.MetricSet, error) {
	cfgwarn.Beta("The docker healthcheck metricset is beta")

	config := docker.Config{}
	if err := base.Module().UnpackConfig(&config); err != nil {
		return nil, err
	}

	client, err := docker.NewDockerClient(base.HostData().URI, config)
	if err != nil {
		return nil, err
	}

	return &MetricSet{
		BaseMetricSet: base,
		dockerClient:  client,
	}, nil
}

// Fetch returns a list of all containers as events.
// This is based on https://docs.docker.com/engine/reference/api/docker_remote_api_v1.24/#/list-containers.
func (m *MetricSet) Fetch() ([]common.MapStr, error) {
	// Fetch a list of all containers.
	containers, err := m.dockerClient.ListContainers(dc.ListContainersOptions{})
	if err != nil {
		return nil, err
	}
	return eventsMapping(containers, m), nil
}