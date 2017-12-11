package util

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"filebeat/input/file"
	"libbeat/common"
)

func TestNewData(t *testing.T) {
	data := NewData()

	assert.False(t, data.HasEvent())
	assert.False(t, data.HasState())

	data.SetState(file.State{Source: "-"})

	assert.False(t, data.HasEvent())
	assert.True(t, data.HasState())

	data.Event.Fields = common.MapStr{}

	assert.True(t, data.HasEvent())
	assert.True(t, data.HasState())
}

func TestGetEvent(t *testing.T) {
	data := NewData()
	data.Event.Fields = common.MapStr{"hello": "world"}
	out := common.MapStr{"hello": "world"}
	assert.Equal(t, out, data.GetEvent().Fields)
}
