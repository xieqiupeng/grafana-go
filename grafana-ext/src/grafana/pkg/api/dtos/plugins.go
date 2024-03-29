package dtos

import (
	"grafana/pkg/components/simplejson"
	"grafana/pkg/plugins"
)

type PluginSetting struct {
	Name          string                      `json:"name"`
	Type          string                      `json:"type"`
	Id            string                      `json:"id"`
	Enabled       bool                        `json:"enabled"`
	Pinned        bool                        `json:"pinned"`
	Module        string                      `json:"module"`
	BaseUrl       string                      `json:"baseUrl"`
	Info          *plugins.PluginInfo         `json:"info"`
	Includes      []*plugins.PluginInclude    `json:"includes"`
	Dependencies  *plugins.PluginDependencies `json:"dependencies"`
	JsonData      map[string]interface{}      `json:"jsonData"`
	DefaultNavUrl string                      `json:"defaultNavUrl"`

	LatestVersion string `json:"latestVersion"`
	HasUpdate     bool   `json:"hasUpdate"`
	State         string `json:"state"`
}

type PluginListItem struct {
	Name          string              `json:"name"`
	Type          string              `json:"type"`
	Id            string              `json:"id"`
	Enabled       bool                `json:"enabled"`
	Pinned        bool                `json:"pinned"`
	Info          *plugins.PluginInfo `json:"info"`
	LatestVersion string              `json:"latestVersion"`
	HasUpdate     bool                `json:"hasUpdate"`
	DefaultNavUrl string              `json:"defaultNavUrl"`
	State         string              `json:"state"`
}

type PluginList []PluginListItem

func (slice PluginList) Len() int {
	return len(slice)
}

func (slice PluginList) Less(i, j int) bool {
	return slice[i].Name < slice[j].Name
}

func (slice PluginList) Swap(i, j int) {
	slice[i], slice[j] = slice[j], slice[i]
}

type ImportDashboardCommand struct {
	PluginId  string                         `json:"pluginId"`
	Path      string                         `json:"path"`
	Overwrite bool                           `json:"overwrite"`
	Dashboard *simplejson.Json               `json:"dashboard"`
	Inputs    []plugins.ImportDashboardInput `json:"inputs"`
}
