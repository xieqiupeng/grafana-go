package api

import (
	"context"

	"grafana/pkg/api/dtos"
	"grafana/pkg/bus"
	"grafana/pkg/components/simplejson"
	"grafana/pkg/middleware"
	"grafana/pkg/models"
	"grafana/pkg/tsdb"
	"grafana/pkg/tsdb/testdata"
	"grafana/pkg/util"
)

// POST /api/tsdb/query
func QueryMetrics(c *middleware.Context, reqDto dtos.MetricRequest) Response {
	timeRange := tsdb.NewTimeRange(reqDto.From, reqDto.To)

	if len(reqDto.Queries) == 0 {
		return ApiError(400, "No queries found in query", nil)
	}

	dsId, err := reqDto.Queries[0].Get("datasourceId").Int64()
	if err != nil {
		return ApiError(400, "Query missing datasourceId", nil)
	}

	dsQuery := models.GetDataSourceByIdQuery{Id: dsId, OrgId: c.OrgId}
	if err := bus.Dispatch(&dsQuery); err != nil {
		return ApiError(500, "failed to fetch data source", err)
	}

	request := &tsdb.TsdbQuery{TimeRange: timeRange}

	for _, query := range reqDto.Queries {
		request.Queries = append(request.Queries, &tsdb.Query{
			RefId:         query.Get("refId").MustString("A"),
			MaxDataPoints: query.Get("maxDataPoints").MustInt64(100),
			IntervalMs:    query.Get("intervalMs").MustInt64(1000),
			Model:         query,
			DataSource:    dsQuery.Result,
		})
	}

	resp, err := tsdb.HandleRequest(context.Background(), dsQuery.Result, request)
	if err != nil {
		return ApiError(500, "Metric request error", err)
	}

	statusCode := 200
	for _, res := range resp.Results {
		if res.Error != nil {
			res.ErrorString = res.Error.Error()
			resp.Message = res.ErrorString
			statusCode = 500
		}
	}

	return Json(statusCode, &resp)
}

// GET /api/tsdb/testdata/scenarios
func GetTestDataScenarios(c *middleware.Context) Response {
	result := make([]interface{}, 0)

	for _, scenario := range testdata.ScenarioRegistry {
		result = append(result, map[string]interface{}{
			"id":          scenario.Id,
			"name":        scenario.Name,
			"description": scenario.Description,
			"stringInput": scenario.StringInput,
		})
	}

	return Json(200, &result)
}

// Genereates a index out of range error
func GenerateError(c *middleware.Context) Response {
	var array []string
	return Json(200, array[20])
}

// GET /api/tsdb/testdata/gensql
func GenerateSqlTestData(c *middleware.Context) Response {
	if err := bus.Dispatch(&models.InsertSqlTestDataCommand{}); err != nil {
		return ApiError(500, "Failed to insert test data", err)
	}

	return Json(200, &util.DynMap{"message": "OK"})
}

// GET /api/tsdb/testdata/random-walk
func GetTestDataRandomWalk(c *middleware.Context) Response {
	from := c.Query("from")
	to := c.Query("to")
	intervalMs := c.QueryInt64("intervalMs")

	timeRange := tsdb.NewTimeRange(from, to)
	request := &tsdb.TsdbQuery{TimeRange: timeRange}

	dsInfo := &models.DataSource{Type: "grafana-testdata-datasource"}
	request.Queries = append(request.Queries, &tsdb.Query{
		RefId:      "A",
		IntervalMs: intervalMs,
		Model: simplejson.NewFromAny(&util.DynMap{
			"scenario": "random_walk",
		}),
		DataSource: dsInfo,
	})

	resp, err := tsdb.HandleRequest(context.Background(), dsInfo, request)
	if err != nil {
		return ApiError(500, "Metric request error", err)
	}

	return Json(200, &resp)
}
