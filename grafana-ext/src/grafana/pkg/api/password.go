package api

import (
	"grafana/pkg/api/dtos"
	"grafana/pkg/bus"
	"grafana/pkg/middleware"
	m "grafana/pkg/models"
	"grafana/pkg/util"
)

func SendResetPasswordEmail(c *middleware.Context, form dtos.SendResetPasswordEmailForm) Response {
	userQuery := m.GetUserByLoginQuery{LoginOrEmail: form.UserOrEmail}

	if err := bus.Dispatch(&userQuery); err != nil {
		c.Logger.Info("Requested password reset for user that was not found", "user", userQuery.LoginOrEmail)
		return ApiError(200, "Email sent", err)
	}

	emailCmd := m.SendResetPasswordEmailCommand{User: userQuery.Result}
	if err := bus.Dispatch(&emailCmd); err != nil {
		return ApiError(500, "Failed to send email", err)
	}

	return ApiSuccess("Email sent")
}

func ResetPassword(c *middleware.Context, form dtos.ResetUserPasswordForm) Response {
	query := m.ValidateResetPasswordCodeQuery{Code: form.Code}

	if err := bus.Dispatch(&query); err != nil {
		if err == m.ErrInvalidEmailCode {
			return ApiError(400, "Invalid or expired reset password code", nil)
		}
		return ApiError(500, "Unknown error validating email code", err)
	}

	if form.NewPassword != form.ConfirmPassword {
		return ApiError(400, "Passwords do not match", nil)
	}

	cmd := m.ChangeUserPasswordCommand{}
	cmd.UserId = query.Result.Id
	cmd.NewPassword = util.EncodePassword(form.NewPassword, query.Result.Salt)

	if err := bus.Dispatch(&cmd); err != nil {
		return ApiError(500, "Failed to change user password", err)
	}

	return ApiSuccess("User password changed")
}
