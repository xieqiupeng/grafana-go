package provisioning

import (
	"grafana/pkg/log"
	"grafana/pkg/services/provisioning/datasources"
)

var (
	logger log.Logger = log.New("services.provisioning")
)

func StartUp(datasourcePath string) error {
	return datasources.Provision(datasourcePath)
}
