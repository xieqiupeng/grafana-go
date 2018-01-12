package api

import (
	"strconv"

	"grafana/pkg/bus"
	"grafana/pkg/metrics"
	"grafana/pkg/middleware"
	"grafana/pkg/services/search"
)

func Search(c *middleware.Context) {
	query := c.Query("query")
	tags := c.QueryStrings("tag")
	starred := c.Query("starred")
	limit := c.QueryInt("limit")

	if limit == 0 {
		limit = 1000
	}

	dbids := make([]int, 0)
	for _, id := range c.QueryStrings("dashboardIds") {
		dashboardId, err := strconv.Atoi(id)
		if err == nil {
			dbids = append(dbids, dashboardId)
		}
	}

	searchQuery := search.Query{
		Title:        query,
		Tags:         tags,
		UserId:       c.UserId,
		Limit:        limit,
		IsStarred:    starred == "true",
		OrgId:        c.OrgId,
		DashboardIds: dbids,
	}

	err := bus.Dispatch(&searchQuery)
	if err != nil {
		c.JsonApiErr(500, "Search failed", err)
		return
	}

	c.TimeRequest(metrics.M_Api_Dashboard_Search)
	c.JSON(200, searchQuery.Result)
}
