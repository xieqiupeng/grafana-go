package api

import (
	"fmt"
	"strings"

	"grafana/pkg/api/dtos"
	"grafana/pkg/bus"
	"grafana/pkg/middleware"
	m "grafana/pkg/models"
	"grafana/pkg/plugins"
	"grafana/pkg/setting"
)

func setIndexViewData(c *middleware.Context) (*dtos.IndexViewData, error) {
	settings, err := getFrontendSettingsMap(c)
	if err != nil {
		return nil, err
	}

	prefsQuery := m.GetPreferencesWithDefaultsQuery{OrgId: c.OrgId, UserId: c.UserId}
	if err := bus.Dispatch(&prefsQuery); err != nil {
		return nil, err
	}
	prefs := prefsQuery.Result

	// Read locale from acccept-language
	acceptLang := c.Req.Header.Get("Accept-Language")
	locale := "en-US"

	if len(acceptLang) > 0 {
		parts := strings.Split(acceptLang, ",")
		locale = parts[0]
	}

	appUrl := setting.AppUrl
	appSubUrl := setting.AppSubUrl

	// special case when doing localhost call from phantomjs
	if c.IsRenderCall {
		appUrl = fmt.Sprintf("%s://localhost:%s", setting.Protocol, setting.HttpPort)
		appSubUrl = ""
		settings["appSubUrl"] = ""
	}

	var data = dtos.IndexViewData{
		User: &dtos.CurrentUser{
			Id:             c.UserId,
			IsSignedIn:     c.IsSignedIn,
			Login:          c.Login,
			Email:          c.Email,
			Name:           c.Name,
			OrgId:          c.OrgId,
			OrgName:        c.OrgName,
			OrgRole:        c.OrgRole,
			GravatarUrl:    dtos.GetGravatarUrl(c.Email),
			IsGrafanaAdmin: c.IsGrafanaAdmin,
			LightTheme:     prefs.Theme == "light",
			Timezone:       prefs.Timezone,
			Locale:         locale,
			HelpFlags1:     c.HelpFlags1,
		},
		Settings:                settings,
		AppUrl:                  appUrl,
		AppSubUrl:               appSubUrl,
		GoogleAnalyticsId:       setting.GoogleAnalyticsId,
		GoogleTagManagerId:      setting.GoogleTagManagerId,
		BuildVersion:            setting.BuildVersion,
		BuildCommit:             setting.BuildCommit,
		NewGrafanaVersion:       plugins.GrafanaLatestVersion,
		NewGrafanaVersionExists: plugins.GrafanaHasUpdate,
	}

	if setting.DisableGravatar {
		data.User.GravatarUrl = setting.AppSubUrl + "/public/img/transparent.png"
	}

	if len(data.User.Name) == 0 {
		data.User.Name = data.User.Login
	}

	themeUrlParam := c.Query("theme")
	if themeUrlParam == "light" {
		data.User.LightTheme = true
	}

	dashboardChildNavs := []*dtos.NavLink{
		{Text: "主页", Url: setting.AppSubUrl + "/"},
		{Text: "播放列表", Url: setting.AppSubUrl + "/playlists"},
		{Text: "快照", Url: setting.AppSubUrl + "/dashboard/snapshots"},
	}

	if c.OrgRole == m.ROLE_ADMIN || c.OrgRole == m.ROLE_EDITOR {
		dashboardChildNavs = append(dashboardChildNavs, &dtos.NavLink{Divider: true})
		dashboardChildNavs = append(dashboardChildNavs, &dtos.NavLink{Text: "新建", Icon: "fa fa-plus", Url: setting.AppSubUrl + "/dashboard/new"})
		dashboardChildNavs = append(dashboardChildNavs, &dtos.NavLink{Text: "导入", Icon: "fa fa-download", Url: setting.AppSubUrl + "/dashboard/new/?editview=import"})
	}

	data.MainNavLinks = append(data.MainNavLinks, &dtos.NavLink{
		Text:     "仪表板",
		Icon:     "icon-gf icon-gf-dashboard",
		Url:      setting.AppSubUrl + "/",
		Children: dashboardChildNavs,
	})

	if setting.AlertingEnabled && (c.OrgRole == m.ROLE_ADMIN || c.OrgRole == m.ROLE_EDITOR) {
		alertChildNavs := []*dtos.NavLink{
			{Text: "告警列表", Url: setting.AppSubUrl + "/alerting/list"},
			{Text: "通知渠道", Url: setting.AppSubUrl + "/alerting/notifications"},
		}

		data.MainNavLinks = append(data.MainNavLinks, &dtos.NavLink{
			Text:     "告警",
			Icon:     "icon-gf icon-gf-alert",
			Url:      setting.AppSubUrl + "/alerting/list",
			Children: alertChildNavs,
		})
	}

	if c.OrgRole == m.ROLE_ADMIN {
		data.MainNavLinks = append(data.MainNavLinks, &dtos.NavLink{
			Text: "数据源",
			Icon: "icon-gf icon-gf-datasources",
			Url:  setting.AppSubUrl + "/datasources",
		})

		data.MainNavLinks = append(data.MainNavLinks, &dtos.NavLink{
			Text: "插件",
			Icon: "icon-gf icon-gf-apps",
			Url:  setting.AppSubUrl + "/plugins",
		})
	}

	enabledPlugins, err := plugins.GetEnabledPlugins(c.OrgId)
	if err != nil {
		return nil, err
	}

	for _, plugin := range enabledPlugins.Apps {
		if plugin.Pinned {
			appLink := &dtos.NavLink{
				Text: plugin.Name,
				Url:  plugin.DefaultNavUrl,
				Img:  plugin.Info.Logos.Small,
			}

			for _, include := range plugin.Includes {
				if !c.HasUserRole(include.Role) {
					continue
				}

				if include.Type == "page" && include.AddToNav {
					link := &dtos.NavLink{
						Url:  setting.AppSubUrl + "/plugins/" + plugin.Id + "/page/" + include.Slug,
						Text: include.Name,
					}
					appLink.Children = append(appLink.Children, link)
				}

				if include.Type == "dashboard" && include.AddToNav {
					link := &dtos.NavLink{
						Url:  setting.AppSubUrl + "/dashboard/db/" + include.Slug,
						Text: include.Name,
					}
					appLink.Children = append(appLink.Children, link)
				}
			}

			if len(appLink.Children) > 0 && c.OrgRole == m.ROLE_ADMIN {
				appLink.Children = append(appLink.Children, &dtos.NavLink{Divider: true})
				appLink.Children = append(appLink.Children, &dtos.NavLink{Text: "Plugin Config", Icon: "fa fa-cog", Url: setting.AppSubUrl + "/plugins/" + plugin.Id + "/edit"})
			}

			if len(appLink.Children) > 0 {
				data.MainNavLinks = append(data.MainNavLinks, appLink)
			}
		}
	}

	if c.IsGrafanaAdmin {
		data.MainNavLinks = append(data.MainNavLinks, &dtos.NavLink{
			Text: "管理员",
			Icon: "fa fa-fw fa-cogs",
			Url:  setting.AppSubUrl + "/admin",
			Children: []*dtos.NavLink{
				{Text: "全局用户", Url: setting.AppSubUrl + "/admin/users"},
				{Text: "全局组织", Url: setting.AppSubUrl + "/admin/orgs"},
				{Text: "服务器设置", Url: setting.AppSubUrl + "/admin/settings"},
				{Text: "服务器统计", Url: setting.AppSubUrl + "/admin/stats"},
			},
		})
	}

	return &data, nil
}

func Index(c *middleware.Context) {
	if data, err := setIndexViewData(c); err != nil {
		c.Handle(500, "Failed to get settings", err)
		return
	} else {
		c.HTML(200, "index", data)
	}
}

func NotFoundHandler(c *middleware.Context) {
	if c.IsApiRequest() {
		c.JsonApiErr(404, "Not found", nil)
		return
	}

	if data, err := setIndexViewData(c); err != nil {
		c.Handle(500, "Failed to get settings", err)
		return
	} else {
		c.HTML(404, "index", data)
	}
}
