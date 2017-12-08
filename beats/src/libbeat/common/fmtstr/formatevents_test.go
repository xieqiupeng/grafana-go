package fmtstr

import (
	"testing"
	"time"

	"github.com/stretchr/testify/assert"

	"libbeat/beat"
	"libbeat/common"
)

func TestEventFormatString(t *testing.T) {
	tests := []struct {
		title    string
		format   string
		event    beat.Event
		expected string
		fields   []string
	}{
		{
			"no fields configured",
			"format string",
			beat.Event{},
			"format string",
			nil,
		},
		{
			"expand event field",
			"%{[key]}",
			beat.Event{Fields: common.MapStr{"key": "value"}},
			"value",
			[]string{"key"},
		},
		{
			"expand with default",
			"%{[key]:default}",
			beat.Event{Fields: common.MapStr{}},
			"default",
			nil,
		},
		{
			"expand nested event field",
			"%{[nested.key]}",
			beat.Event{Fields: common.MapStr{"nested": common.MapStr{"key": "value"}}},
			"value",
			[]string{"nested.key"},
		},
		{
			"expand nested event field (alt. syntax)",
			"%{[nested][key]}",
			beat.Event{Fields: common.MapStr{"nested": common.MapStr{"key": "value"}}},
			"value",
			[]string{"nested.key"},
		},
		{
			"multiple event fields",
			"%{[key1]} - %{[key2]}",
			beat.Event{Fields: common.MapStr{"key1": "v1", "key2": "v2"}},
			"v1 - v2",
			[]string{"key1", "key2"},
		},
		{
			"same fields",
			"%{[key]} - %{[key]}",
			beat.Event{Fields: common.MapStr{"key": "value"}},
			"value - value",
			[]string{"key"},
		},
		{
			"same fields with default (first)",
			"%{[key]:default} - %{[key]}",
			beat.Event{Fields: common.MapStr{"key": "value"}},
			"value - value",
			[]string{"key"},
		},
		{
			"same fields with default (second)",
			"%{[key]} - %{[key]:default}",
			beat.Event{Fields: common.MapStr{"key": "value"}},
			"value - value",
			[]string{"key"},
		},
		{
			"test timestamp formatter",
			"%{[key]}: %{+YYYY.MM.dd}",
			beat.Event{
				Timestamp: time.Date(2015, 5, 1, 20, 12, 34, 0, time.Local),
				Fields: common.MapStr{
					"key": "timestamp",
				},
			},
			"timestamp: 2015.05.01",
			[]string{"key"},
		},
		{
			"test timestamp formatter",
			"%{[@timestamp]}: %{+YYYY.MM.dd}",
			beat.Event{
				Timestamp: time.Date(2015, 5, 1, 20, 12, 34, 0, time.Local),
				Fields: common.MapStr{
					"key": "timestamp",
				},
			},
			"2015-05-01T20:12:34.000Z: 2015.05.01",
			[]string{"@timestamp"},
		},
	}

	for i, test := range tests {
		t.Logf("test(%v): %v", i, test.title)

		fs, err := CompileEvent(test.format)
		if err != nil {
			t.Error(err)
			continue
		}

		actual, err := fs.Run(&test.event)

		assert.NoError(t, err)
		assert.Equal(t, test.expected, actual)
		assert.Equal(t, test.fields, fs.Fields())
	}
}

func TestEventFormatStringErrors(t *testing.T) {
	tests := []struct {
		title          string
		format         string
		expectCompiles bool
		event          beat.Event
	}{
		{
			"empty field",
			"%{[]}",
			false, beat.Event{},
		},
		{
			"field not closed",
			"%{[field}",
			false, beat.Event{},
		},
		{
			"no field accessor",
			"%{field}",
			false, beat.Event{},
		},
		{
			"unknown operator",
			"%{[field]:?fail}",
			false, beat.Event{},
		},
		{
			"too many operators",
			"%{[field]:a:b}",
			false, beat.Event{},
		},
		{
			"invalid timestamp formatter",
			"%{+abc}",
			false, beat.Event{},
		},
		{
			"missing required field",
			"%{[key]}",
			true,
			beat.Event{Fields: common.MapStr{}},
		},
	}

	for i, test := range tests {
		t.Logf("run (%v): %v", i, test.title)

		fs, err := CompileEvent(test.format)
		if !test.expectCompiles {
			assert.Error(t, err)
			continue
		}
		if err != nil {
			t.Error(err)
			continue
		}

		_, err = fs.Run(&test.event)
		assert.Error(t, err)
	}
}

func TestEventFormatStringFromConfig(t *testing.T) {
	tests := []struct {
		v        interface{}
		event    beat.Event
		expected string
	}{
		{
			"plain string",
			beat.Event{Fields: common.MapStr{}},
			"plain string",
		},
		{
			100,
			beat.Event{Fields: common.MapStr{}},
			"100",
		},
		{
			true,
			beat.Event{Fields: common.MapStr{}},
			"true",
		},
		{
			"%{[key]}",
			beat.Event{Fields: common.MapStr{"key": "value"}},
			"value",
		},
	}

	for i, test := range tests {
		t.Logf("run (%v): %v -> %v", i, test.v, test.expected)

		config, err := common.NewConfigFrom(common.MapStr{
			"test": test.v,
		})
		if err != nil {
			t.Error(err)
			continue
		}

		testConfig := struct {
			Test *EventFormatString `config:"test"`
		}{}
		err = config.Unpack(&testConfig)
		if err != nil {
			t.Error(err)
			continue
		}

		actual, err := testConfig.Test.Run(&test.event)
		if err != nil {
			t.Error(err)
			continue
		}

		assert.Equal(t, test.expected, actual)
	}
}
