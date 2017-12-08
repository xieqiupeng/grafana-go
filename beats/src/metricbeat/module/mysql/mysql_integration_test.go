// +build integration

package mysql

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"libbeat/tests/compose"
	_ "metricbeat/mb/testing"
)

func TestNewDB(t *testing.T) {
	compose.EnsureUp(t, "mysql")

	db, err := NewDB(GetMySQLEnvDSN())
	assert.NoError(t, err)

	err = db.Ping()
	assert.NoError(t, err)
}
