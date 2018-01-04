package mysql

import (
	"container/list"
	"context"
	"database/sql"
	"fmt"
	"strconv"

	"time"

	"github.com/go-sql-driver/mysql"
	"github.com/go-xorm/core"
	"github.com/grafana/grafana/pkg/components/null"
	"github.com/grafana/grafana/pkg/log"
	"github.com/grafana/grafana/pkg/models"
	"github.com/grafana/grafana/pkg/tsdb"
)

type MysqlQueryEndpoint struct {
	sqlEngine tsdb.SqlEngine
	log       log.Logger
}

func init() {
	tsdb.RegisterTsdbQueryEndpoint("mysql", NewMysqlQueryEndpoint)
}

func NewMysqlQueryEndpoint(datasource *models.DataSource) (tsdb.TsdbQueryEndpoint, error) {
	endpoint := &MysqlQueryEndpoint{
		log: log.New("tsdb.mysql"),
	}

	endpoint.sqlEngine = &tsdb.DefaultSqlEngine{
		MacroEngine: NewMysqlMacroEngine(),
	}

	cnnstr := fmt.Sprintf("%s:%s@%s(%s)/%s?collation=utf8mb4_unicode_ci&parseTime=true&loc=UTC&allowNativePasswords=true",
		datasource.User,
		datasource.Password,
		"tcp",
		datasource.Url,
		datasource.Database,
	)
	endpoint.log.Debug("getEngine", "connection", cnnstr)

	if err := endpoint.sqlEngine.InitEngine("mysql", datasource, cnnstr); err != nil {
		return nil, err
	}

	return endpoint, nil
}

// Query is the main function for the MysqlExecutor
func (e *MysqlQueryEndpoint) Query(ctx context.Context, dsInfo *models.DataSource, tsdbQuery *tsdb.TsdbQuery) (*tsdb.Response, error) {
	return e.sqlEngine.Query(ctx, dsInfo, tsdbQuery, e.transformToTimeSeries, e.transformToTable)
}

func (e MysqlQueryEndpoint) transformToTable(query *tsdb.Query, rows *core.Rows, result *tsdb.QueryResult) error {
	columnNames, err := rows.Columns()
	columnCount := len(columnNames)

	if err != nil {
		return err
	}

	table := &tsdb.Table{
		Columns: make([]tsdb.TableColumn, columnCount),
		Rows:    make([]tsdb.RowValues, 0),
	}

	for i, name := range columnNames {
		table.Columns[i].Text = name
	}

	columnTypes, err := rows.ColumnTypes()
	if err != nil {
		return err
	}

	rowLimit := 1000000
	rowCount := 0

	for ; rows.Next(); rowCount++ {
		if rowCount > rowLimit {
			return fmt.Errorf("MySQL query row limit exceeded, limit %d", rowLimit)
		}

		values, err := e.getTypedRowData(columnTypes, rows)
		if err != nil {
			return err
		}

		table.Rows = append(table.Rows, values)
	}

	result.Tables = append(result.Tables, table)
	result.Meta.Set("rowCount", rowCount)
	return nil
}

func (e MysqlQueryEndpoint) getTypedRowData(types []*sql.ColumnType, rows *core.Rows) (tsdb.RowValues, error) {
	values := make([]interface{}, len(types))

	for i, stype := range types {
		e.log.Debug("type", "type", stype)
		switch stype.DatabaseTypeName() {
		case mysql.FieldTypeNameTiny:
			values[i] = new(int8)
		case mysql.FieldTypeNameInt24:
			values[i] = new(int32)
		case mysql.FieldTypeNameShort:
			values[i] = new(int16)
		case mysql.FieldTypeNameVarString:
			values[i] = new(string)
		case mysql.FieldTypeNameVarChar:
			values[i] = new(string)
		case mysql.FieldTypeNameLong:
			values[i] = new(int)
		case mysql.FieldTypeNameLongLong:
			values[i] = new(int64)
		case mysql.FieldTypeNameDouble:
			values[i] = new(float64)
		case mysql.FieldTypeNameDecimal:
			values[i] = new(float32)
		case mysql.FieldTypeNameNewDecimal:
			values[i] = new(float64)
		case mysql.FieldTypeNameFloat:
			values[i] = new(float64)
		case mysql.FieldTypeNameTimestamp:
			values[i] = new(time.Time)
		case mysql.FieldTypeNameDateTime:
			values[i] = new(time.Time)
		case mysql.FieldTypeNameTime:
			values[i] = new(string)
		case mysql.FieldTypeNameYear:
			values[i] = new(int16)
		case mysql.FieldTypeNameNULL:
			values[i] = nil
		case mysql.FieldTypeNameBit:
			values[i] = new([]byte)
		case mysql.FieldTypeNameBLOB:
			values[i] = new(string)
		case mysql.FieldTypeNameTinyBLOB:
			values[i] = new(string)
		case mysql.FieldTypeNameMediumBLOB:
			values[i] = new(string)
		case mysql.FieldTypeNameLongBLOB:
			values[i] = new(string)
		case mysql.FieldTypeNameString:
			values[i] = new(string)
		case mysql.FieldTypeNameDate:
			values[i] = new(string)
		default:
			return nil, fmt.Errorf("Database type %s not supported", stype.DatabaseTypeName())
		}
	}

	if err := rows.Scan(values...); err != nil {
		return nil, err
	}

	return values, nil
}

func (e MysqlQueryEndpoint) transformToTimeSeries(query *tsdb.Query, rows *core.Rows, result *tsdb.QueryResult) error {
	pointsBySeries := make(map[string]*tsdb.TimeSeries)
	seriesByQueryOrder := list.New()
	columnNames, err := rows.Columns()

	if err != nil {
		return err
	}

	rowData := NewStringStringScan(columnNames)
	rowLimit := 1000000
	rowCount := 0

	for ; rows.Next(); rowCount++ {
		if rowCount > rowLimit {
			return fmt.Errorf("MySQL query row limit exceeded, limit %d", rowLimit)
		}

		err := rowData.Update(rows.Rows)
		if err != nil {
			e.log.Error("MySQL response parsing", "error", err)
			return fmt.Errorf("MySQL response parsing error %v", err)
		}

		if rowData.metric == "" {
			rowData.metric = "Unknown"
		}

		if !rowData.time.Valid {
			return fmt.Errorf("Found row with no time value")
		}

		if series, exist := pointsBySeries[rowData.metric]; exist {
			series.Points = append(series.Points, tsdb.TimePoint{rowData.value, rowData.time})
		} else {
			series := &tsdb.TimeSeries{Name: rowData.metric}
			series.Points = append(series.Points, tsdb.TimePoint{rowData.value, rowData.time})
			pointsBySeries[rowData.metric] = series
			seriesByQueryOrder.PushBack(rowData.metric)
		}
	}

	for elem := seriesByQueryOrder.Front(); elem != nil; elem = elem.Next() {
		key := elem.Value.(string)
		result.Series = append(result.Series, pointsBySeries[key])
	}

	result.Meta.Set("rowCount", rowCount)
	return nil
}

type stringStringScan struct {
	rowPtrs     []interface{}
	rowValues   []string
	columnNames []string
	columnCount int

	time   null.Float
	value  null.Float
	metric string
}

func NewStringStringScan(columnNames []string) *stringStringScan {
	s := &stringStringScan{
		columnCount: len(columnNames),
		columnNames: columnNames,
		rowPtrs:     make([]interface{}, len(columnNames)),
		rowValues:   make([]string, len(columnNames)),
	}

	for i := 0; i < s.columnCount; i++ {
		s.rowPtrs[i] = new(sql.RawBytes)
	}

	return s
}

func (s *stringStringScan) Update(rows *sql.Rows) error {
	if err := rows.Scan(s.rowPtrs...); err != nil {
		return err
	}

	s.time = null.FloatFromPtr(nil)
	s.value = null.FloatFromPtr(nil)

	for i := 0; i < s.columnCount; i++ {
		if rb, ok := s.rowPtrs[i].(*sql.RawBytes); ok {
			s.rowValues[i] = string(*rb)

			switch s.columnNames[i] {
			case "time_sec":
				if sec, err := strconv.ParseInt(s.rowValues[i], 10, 64); err == nil {
					s.time = null.FloatFrom(float64(sec * 1000))
				}
			case "value":
				if value, err := strconv.ParseFloat(s.rowValues[i], 64); err == nil {
					s.value = null.FloatFrom(value)
				}
			case "metric":
				s.metric = s.rowValues[i]
			}

			*rb = nil // reset pointer to discard current value to avoid a bug
		} else {
			return fmt.Errorf("Cannot convert index %d column %s to type *sql.RawBytes", i, s.columnNames[i])
		}
	}
	return nil
}
