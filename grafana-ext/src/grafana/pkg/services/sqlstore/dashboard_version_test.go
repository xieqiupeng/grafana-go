package sqlstore

import (
	"reflect"
	"testing"

	. "github.com/smartystreets/goconvey/convey"

	"grafana/pkg/components/simplejson"
	m "grafana/pkg/models"
	"grafana/pkg/setting"
)

func updateTestDashboard(dashboard *m.Dashboard, data map[string]interface{}) {
	data["title"] = dashboard.Title

	saveCmd := m.SaveDashboardCommand{
		OrgId:     dashboard.OrgId,
		Overwrite: true,
		Dashboard: simplejson.NewFromAny(data),
	}

	err := SaveDashboard(&saveCmd)
	So(err, ShouldBeNil)
}

func TestGetDashboardVersion(t *testing.T) {
	Convey("Testing dashboard version retrieval", t, func() {
		InitTestDB(t)

		Convey("Get a Dashboard ID and version ID", func() {
			savedDash := insertTestDashboard("test dash 26", 1, "diff")

			query := m.GetDashboardVersionQuery{
				DashboardId: savedDash.Id,
				Version:     savedDash.Version,
				OrgId:       1,
			}

			err := GetDashboardVersion(&query)
			So(err, ShouldBeNil)
			So(savedDash.Id, ShouldEqual, query.DashboardId)
			So(savedDash.Version, ShouldEqual, query.Version)

			dashCmd := m.GetDashboardQuery{
				OrgId: savedDash.OrgId,
				Slug:  savedDash.Slug,
			}

			err = GetDashboard(&dashCmd)
			So(err, ShouldBeNil)
			eq := reflect.DeepEqual(dashCmd.Result.Data, query.Result.Data)
			So(eq, ShouldEqual, true)
		})

		Convey("Attempt to get a version that doesn't exist", func() {
			query := m.GetDashboardVersionQuery{
				DashboardId: int64(999),
				Version:     123,
				OrgId:       1,
			}

			err := GetDashboardVersion(&query)
			So(err, ShouldNotBeNil)
			So(err, ShouldEqual, m.ErrDashboardVersionNotFound)
		})
	})
}

func TestGetDashboardVersions(t *testing.T) {
	Convey("Testing dashboard versions retrieval", t, func() {
		InitTestDB(t)
		savedDash := insertTestDashboard("test dash 43", 1, "diff-all")

		Convey("Get all versions for a given Dashboard ID", func() {
			query := m.GetDashboardVersionsQuery{DashboardId: savedDash.Id, OrgId: 1}

			err := GetDashboardVersions(&query)
			So(err, ShouldBeNil)
			So(len(query.Result), ShouldEqual, 1)
		})

		Convey("Attempt to get the versions for a non-existent Dashboard ID", func() {
			query := m.GetDashboardVersionsQuery{DashboardId: int64(999), OrgId: 1}

			err := GetDashboardVersions(&query)
			So(err, ShouldNotBeNil)
			So(err, ShouldEqual, m.ErrNoVersionsForDashboardId)
			So(len(query.Result), ShouldEqual, 0)
		})

		Convey("Get all versions for an updated dashboard", func() {
			updateTestDashboard(savedDash, map[string]interface{}{
				"tags": "different-tag",
			})

			query := m.GetDashboardVersionsQuery{DashboardId: savedDash.Id, OrgId: 1}
			err := GetDashboardVersions(&query)

			So(err, ShouldBeNil)
			So(len(query.Result), ShouldEqual, 2)
		})
	})
}

func TestDeleteExpiredVersions(t *testing.T) {
	Convey("Testing dashboard versions clean up", t, func() {
		InitTestDB(t)
		versionsToKeep := 5
		versionsToWrite := 10
		setting.DashboardVersionsToKeep = versionsToKeep

		savedDash := insertTestDashboard("test dash 53", 1, "diff-all")
		for i := 0; i < versionsToWrite-1; i++ {
			updateTestDashboard(savedDash, map[string]interface{}{
				"tags": "different-tag",
			})
		}

		Convey("Clean up old dashboard versions", func() {
			err := DeleteExpiredVersions(&m.DeleteExpiredVersionsCommand{})
			So(err, ShouldBeNil)

			query := m.GetDashboardVersionsQuery{DashboardId: savedDash.Id, OrgId: 1}
			GetDashboardVersions(&query)

			So(len(query.Result), ShouldEqual, versionsToKeep)
			// Ensure latest versions were kept
			So(query.Result[versionsToKeep-1].Version, ShouldEqual, versionsToWrite-versionsToKeep+1)
			So(query.Result[0].Version, ShouldEqual, versionsToWrite)
		})

		Convey("Don't delete anything if there're no expired versions", func() {
			setting.DashboardVersionsToKeep = versionsToWrite

			err := DeleteExpiredVersions(&m.DeleteExpiredVersionsCommand{})
			So(err, ShouldBeNil)

			query := m.GetDashboardVersionsQuery{DashboardId: savedDash.Id, OrgId: 1}
			GetDashboardVersions(&query)

			So(len(query.Result), ShouldEqual, versionsToWrite)
		})
	})
}
