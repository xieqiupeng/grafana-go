package sqlstore

import (
	"testing"

	"github.com/go-xorm/xorm"

	. "github.com/smartystreets/goconvey/convey"

	m "grafana/pkg/models"
	"grafana/pkg/services/sqlstore/sqlutil"
)

func InitTestDB(t *testing.T) {
	x, err := xorm.NewEngine(sqlutil.TestDB_Sqlite3.DriverName, sqlutil.TestDB_Sqlite3.ConnStr)
	//x, err := xorm.NewEngine(sqlutil.TestDB_Mysql.DriverName, sqlutil.TestDB_Mysql.ConnStr)
	//x, err := xorm.NewEngine(sqlutil.TestDB_Postgres.DriverName, sqlutil.TestDB_Postgres.ConnStr)

	// x.ShowSQL()

	if err != nil {
		t.Fatalf("Failed to init in memory sqllite3 db %v", err)
	}

	sqlutil.CleanDB(x)

	if err := SetEngine(x); err != nil {
		t.Fatal(err)
	}
}

type Test struct {
	Id   int64
	Name string
}

func TestDataAccess(t *testing.T) {
	Convey("Testing DB", t, func() {
		InitTestDB(t)
		Convey("Can add datasource", func() {
			err := AddDataSource(&m.AddDataSourceCommand{
				OrgId:    10,
				Name:     "laban",
				Type:     m.DS_INFLUXDB,
				Access:   m.DS_ACCESS_DIRECT,
				Url:      "http://test",
				Database: "site",
				ReadOnly: true,
			})

			So(err, ShouldBeNil)

			query := m.GetDataSourcesQuery{OrgId: 10}
			err = GetDataSources(&query)
			So(err, ShouldBeNil)

			So(len(query.Result), ShouldEqual, 1)

			ds := query.Result[0]

			So(ds.OrgId, ShouldEqual, 10)
			So(ds.Database, ShouldEqual, "site")
			So(ds.ReadOnly, ShouldBeTrue)
		})

		Convey("Given a datasource", func() {
			err := AddDataSource(&m.AddDataSourceCommand{
				OrgId:  10,
				Name:   "nisse",
				Type:   m.DS_GRAPHITE,
				Access: m.DS_ACCESS_DIRECT,
				Url:    "http://test",
			})
			So(err, ShouldBeNil)

			query := m.GetDataSourcesQuery{OrgId: 10}
			err = GetDataSources(&query)
			So(err, ShouldBeNil)

			ds := query.Result[0]

			Convey(" updated ", func() {
				cmd := &m.UpdateDataSourceCommand{
					Id:      ds.Id,
					OrgId:   10,
					Name:    "nisse",
					Type:    m.DS_GRAPHITE,
					Access:  m.DS_ACCESS_PROXY,
					Url:     "http://test",
					Version: ds.Version,
				}

				Convey("with same version as source", func() {
					err := UpdateDataSource(cmd)
					So(err, ShouldBeNil)
				})

				Convey("when someone else updated between read and update", func() {
					query := m.GetDataSourcesQuery{OrgId: 10}
					err = GetDataSources(&query)
					So(err, ShouldBeNil)

					ds := query.Result[0]
					intendedUpdate := &m.UpdateDataSourceCommand{
						Id:      ds.Id,
						OrgId:   10,
						Name:    "nisse",
						Type:    m.DS_GRAPHITE,
						Access:  m.DS_ACCESS_PROXY,
						Url:     "http://test",
						Version: ds.Version,
					}

					updateFromOtherUser := &m.UpdateDataSourceCommand{
						Id:      ds.Id,
						OrgId:   10,
						Name:    "nisse",
						Type:    m.DS_GRAPHITE,
						Access:  m.DS_ACCESS_PROXY,
						Url:     "http://test",
						Version: ds.Version,
					}

					err := UpdateDataSource(updateFromOtherUser)
					So(err, ShouldBeNil)

					err = UpdateDataSource(intendedUpdate)
					So(err, ShouldNotBeNil)
				})

				Convey("updating datasource without version", func() {
					cmd := &m.UpdateDataSourceCommand{
						Id:     ds.Id,
						OrgId:  10,
						Name:   "nisse",
						Type:   m.DS_GRAPHITE,
						Access: m.DS_ACCESS_PROXY,
						Url:    "http://test",
					}

					Convey("should not raise errors", func() {
						err := UpdateDataSource(cmd)
						So(err, ShouldBeNil)
					})
				})

				Convey("updating datasource without higher version", func() {
					cmd := &m.UpdateDataSourceCommand{
						Id:      ds.Id,
						OrgId:   10,
						Name:    "nisse",
						Type:    m.DS_GRAPHITE,
						Access:  m.DS_ACCESS_PROXY,
						Url:     "http://test",
						Version: 90000,
					}

					Convey("should not raise errors", func() {
						err := UpdateDataSource(cmd)
						So(err, ShouldBeNil)
					})
				})
			})

			Convey("Can delete datasource by id", func() {
				err := DeleteDataSourceById(&m.DeleteDataSourceByIdCommand{Id: ds.Id, OrgId: ds.OrgId})
				So(err, ShouldBeNil)

				GetDataSources(&query)
				So(len(query.Result), ShouldEqual, 0)
			})

			Convey("Can delete datasource by name", func() {
				err := DeleteDataSourceByName(&m.DeleteDataSourceByNameCommand{Name: ds.Name, OrgId: ds.OrgId})
				So(err, ShouldBeNil)

				GetDataSources(&query)
				So(len(query.Result), ShouldEqual, 0)
			})

			Convey("Can not delete datasource with wrong orgId", func() {
				err := DeleteDataSourceById(&m.DeleteDataSourceByIdCommand{Id: ds.Id, OrgId: 123123})
				So(err, ShouldBeNil)

				GetDataSources(&query)
				So(len(query.Result), ShouldEqual, 1)
			})
		})
	})
}
