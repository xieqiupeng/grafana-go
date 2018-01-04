// +build darwin freebsd linux openbsd windows

package memory

import (
	"libbeat/common"
	"metricbeat/module/system"

	sigar "github.com/elastic/gosigar"
)

type MemStat struct {
	sigar.Mem
	UsedPercent       float64 `json:"used_p"`
	ActualUsedPercent float64 `json:"actual_used_p"`
}

func GetMemory() (*MemStat, error) {
	mem := sigar.Mem{}
	err := mem.Get()
	if err != nil {
		return nil, err
	}

	return &MemStat{Mem: mem}, nil
}

func AddMemPercentage(m *MemStat) {
	if m.Mem.Total == 0 {
		return
	}

	perc := float64(m.Mem.Used) / float64(m.Mem.Total)
	m.UsedPercent = system.Round(perc)

	actualPerc := float64(m.Mem.ActualUsed) / float64(m.Mem.Total)
	m.ActualUsedPercent = system.Round(actualPerc)
}

type SwapStat struct {
	sigar.Swap
	UsedPercent float64 `json:"used_p"`
}

func GetSwap() (*SwapStat, error) {
	swap := sigar.Swap{}
	err := swap.Get()
	if err != nil {
		return nil, err
	}

	return &SwapStat{Swap: swap}, nil
}

func GetMemoryEvent(memStat *MemStat) common.MapStr {
	return common.MapStr{
		"total_long":           memStat.Total,
		"used_long":            memStat.Used,
		"free_long":            memStat.Free,
		"actual_used_long":     memStat.ActualUsed,
		"actual_free_long":     memStat.ActualFree,
		"used_p_double":        memStat.UsedPercent,
		"actual_used_p_double": memStat.ActualUsedPercent,
	}
}

func GetSwapEvent(swapStat *SwapStat) common.MapStr {
	return common.MapStr{
		"total_long":    swapStat.Total,
		"used_long":     swapStat.Used,
		"free_long":     swapStat.Free,
		"used_p_double": swapStat.UsedPercent,
	}
}

func AddSwapPercentage(s *SwapStat) {
	if s.Swap.Total == 0 {
		return
	}

	perc := float64(s.Swap.Used) / float64(s.Swap.Total)
	s.UsedPercent = system.Round(perc)
}
