package dtos

import (
	"time"

	"grafana/pkg/components/simplejson"
)

type DashboardMeta struct {
	IsStarred  bool      `json:"isStarred,omitempty"`
	IsHome     bool      `json:"isHome,omitempty"`
	IsSnapshot bool      `json:"isSnapshot,omitempty"`
	Type       string    `json:"type,omitempty"`
	CanSave    bool      `json:"canSave"`
	CanEdit    bool      `json:"canEdit"`
	CanStar    bool      `json:"canStar"`
	Slug       string    `json:"slug"`
	Expires    time.Time `json:"expires"`
	Created    time.Time `json:"created"`
	Updated    time.Time `json:"updated"`
	UpdatedBy  string    `json:"updatedBy"`
	CreatedBy  string    `json:"createdBy"`
	Version    int       `json:"version"`
}

type DashboardFullWithMeta struct {
	Meta      DashboardMeta    `json:"meta"`
	Dashboard *simplejson.Json `json:"dashboard"`
}

type DashboardRedirect struct {
	RedirectUri string `json:"redirectUri"`
}

type CalculateDiffOptions struct {
	Base     CalculateDiffTarget `json:"base" binding:"Required"`
	New      CalculateDiffTarget `json:"new" binding:"Required"`
	DiffType string              `json:"diffType" binding:"Required"`
}

type CalculateDiffTarget struct {
	DashboardId      int64            `json:"dashboardId"`
	Version          int              `json:"version"`
	UnsavedDashboard *simplejson.Json `json:"unsavedDashboard"`
}

type RestoreDashboardVersionCommand struct {
	Version int `json:"version" binding:"Required"`
}
