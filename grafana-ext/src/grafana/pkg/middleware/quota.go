package middleware

import (
	"fmt"

	"grafana/pkg/bus"
	m "grafana/pkg/models"
	"grafana/pkg/setting"
	"gopkg.in/macaron.v1"
)

func Quota(target string) macaron.Handler {
	return func(c *Context) {
		limitReached, err := QuotaReached(c, target)
		if err != nil {
			c.JsonApiErr(500, "failed to get quota", err)
			return
		}
		if limitReached {
			c.JsonApiErr(403, fmt.Sprintf("%s Quota reached", target), nil)
			return
		}
	}
}

func QuotaReached(c *Context, target string) (bool, error) {
	if !setting.Quota.Enabled {
		return false, nil
	}

	// get the list of scopes that this target is valid for. Org, User, Global
	scopes, err := m.GetQuotaScopes(target)
	if err != nil {
		return false, err
	}

	for _, scope := range scopes {
		c.Logger.Debug("Checking quota", "target", target, "scope", scope)

		switch scope.Name {
		case "global":
			if scope.DefaultLimit < 0 {
				continue
			}
			if scope.DefaultLimit == 0 {
				return true, nil
			}
			if target == "session" {
				usedSessions := getSessionCount()
				if int64(usedSessions) > scope.DefaultLimit {
					c.Logger.Debug("Sessions limit reached", "active", usedSessions, "limit", scope.DefaultLimit)
					return true, nil
				}
				continue
			}
			query := m.GetGlobalQuotaByTargetQuery{Target: scope.Target}
			if err := bus.Dispatch(&query); err != nil {
				return true, err
			}
			if query.Result.Used >= scope.DefaultLimit {
				return true, nil
			}
		case "org":
			if !c.IsSignedIn {
				continue
			}
			query := m.GetOrgQuotaByTargetQuery{OrgId: c.OrgId, Target: scope.Target, Default: scope.DefaultLimit}
			if err := bus.Dispatch(&query); err != nil {
				return true, err
			}
			if query.Result.Limit < 0 {
				continue
			}
			if query.Result.Limit == 0 {
				return true, nil
			}

			if query.Result.Used >= query.Result.Limit {
				return true, nil
			}
		case "user":
			if !c.IsSignedIn || c.UserId == 0 {
				continue
			}
			query := m.GetUserQuotaByTargetQuery{UserId: c.UserId, Target: scope.Target, Default: scope.DefaultLimit}
			if err := bus.Dispatch(&query); err != nil {
				return true, err
			}
			if query.Result.Limit < 0 {
				continue
			}
			if query.Result.Limit == 0 {
				return true, nil
			}

			if query.Result.Used >= query.Result.Limit {
				return true, nil
			}
		}
	}

	return false, nil
}
