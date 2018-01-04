package process

import (
	"strconv"

	"libbeat/common"

	"github.com/elastic/gosigar/cgroup"
)

// cgroupStatsToMap returns a MapStr containing the data from the stats object.
// If stats is nil then nil is returned.
func cgroupStatsToMap(stats *cgroup.Stats) common.MapStr {
	if stats == nil {
		return nil
	}

	cgroup := common.MapStr{}

	// id and path are only available when all subsystems share a common path.
	if stats.ID != "" {
		cgroup["id_string"] = stats.ID
	}
	if stats.Path != "" {
		cgroup["path_string"] = stats.Path
	}

	if cpu := cgroupCPUToMapStr(stats.CPU); cpu != nil {
		cgroup["cpu"] = cpu
	}
	if cpuacct := cgroupCPUAccountingToMapStr(stats.CPUAccounting); cpuacct != nil {
		cgroup["cpuacct"] = cpuacct
	}
	if memory := cgroupMemoryToMapStr(stats.Memory); memory != nil {
		cgroup["memory"] = memory
	}
	if blkio := cgroupBlockIOToMapStr(stats.BlockIO); blkio != nil {
		cgroup["blkio"] = blkio
	}

	return cgroup
}

// cgroupCPUToMapStr returns a MapStr containing CPUSubsystem data. If the
// cpu parameter is nil then nil is returned.
func cgroupCPUToMapStr(cpu *cgroup.CPUSubsystem) common.MapStr {
	if cpu == nil {
		return nil
	}

	return common.MapStr{
		"id_string":   cpu.ID,
		"path_string": cpu.Path,
		"cfs": common.MapStr{
			"period": common.MapStr{
				"us_long": cpu.CFS.PeriodMicros,
			},
			"quota": common.MapStr{
				"us_long": cpu.CFS.QuotaMicros,
			},
			"shares_long": cpu.CFS.Shares,
		},
		"rt": common.MapStr{
			"period": common.MapStr{
				"us_long": cpu.RT.PeriodMicros,
			},
			"runtime": common.MapStr{
				"us_long": cpu.RT.RuntimeMicros,
			},
		},
		"stats": common.MapStr{
			"periods_long": cpu.Stats.Periods,
			"throttled": common.MapStr{
				"periods_long": cpu.Stats.ThrottledPeriods,
				"ns_long":      cpu.Stats.ThrottledTimeNanos,
			},
		},
	}
}

// cgroupCPUAccountingToMapStr returns a MapStr containing
// CPUAccountingSubsystem data. If the cpuacct parameter is nil then nil is
// returned.
func cgroupCPUAccountingToMapStr(cpuacct *cgroup.CPUAccountingSubsystem) common.MapStr {
	if cpuacct == nil {
		return nil
	}

	perCPUUsage := common.MapStr{}
	for i, usage := range cpuacct.UsagePerCPU {
		perCPUUsage[strconv.Itoa(i+1)] = usage
	}

	return common.MapStr{
		"id_string":   cpuacct.ID,
		"path_string": cpuacct.Path,
		"total": common.MapStr{
			"ns_long": cpuacct.TotalNanos,
		},
		"percpu_long": perCPUUsage,
		"stats": common.MapStr{
			"system": common.MapStr{
				"ns_long": cpuacct.Stats.SystemNanos,
			},
			"user": common.MapStr{
				"ns_long": cpuacct.Stats.UserNanos,
			},
		},
	}
}

// cgroupMemoryToMapStr returns a MapStr containing MemorySubsystem data. If the
// memory parameter is nil then nil is returned.
func cgroupMemoryToMapStr(memory *cgroup.MemorySubsystem) common.MapStr {
	if memory == nil {
		return nil
	}

	addMemData := func(key string, m common.MapStr, data cgroup.MemoryData) {
		m[key] = common.MapStr{
			"failures_long": memory.Mem.FailCount,
			"limit": common.MapStr{
				"bytes_long": memory.Mem.Limit,
			},
			"usage": common.MapStr{
				"bytes_long": memory.Mem.Usage,
				"max": common.MapStr{
					"bytes_long": memory.Mem.MaxUsage,
				},
			},
		}
	}

	memMap := common.MapStr{
		"id_string":   memory.ID,
		"path_string": memory.Path,
	}
	addMemData("mem", memMap, memory.Mem)
	addMemData("memsw", memMap, memory.MemSwap)
	addMemData("kmem", memMap, memory.Kernel)
	addMemData("kmem_tcp", memMap, memory.KernelTCP)
	memMap["stats"] = common.MapStr{
		"active_anon": common.MapStr{
			"bytes_long": memory.Stats.ActiveAnon,
		},
		"active_file": common.MapStr{
			"bytes_long": memory.Stats.ActiveFile,
		},
		"cache": common.MapStr{
			"bytes_long": memory.Stats.Cache,
		},
		"hierarchical_memory_limit": common.MapStr{
			"bytes_long": memory.Stats.HierarchicalMemoryLimit,
		},
		"hierarchical_memsw_limit": common.MapStr{
			"bytes_long": memory.Stats.HierarchicalMemswLimit,
		},
		"inactive_anon": common.MapStr{
			"bytes_long": memory.Stats.InactiveAnon,
		},
		"inactive_file": common.MapStr{
			"bytes_long": memory.Stats.InactiveFile,
		},
		"mapped_file": common.MapStr{
			"bytes_long": memory.Stats.MappedFile,
		},
		"page_faults_long":       memory.Stats.PageFaults,
		"major_page_faults_long": memory.Stats.MajorPageFaults,
		"pages_in_long":          memory.Stats.PagesIn,
		"pages_out_long":         memory.Stats.PagesOut,
		"rss": common.MapStr{
			"bytes_long": memory.Stats.RSS,
		},
		"rss_huge": common.MapStr{
			"bytes_long": memory.Stats.RSSHuge,
		},
		"swap": common.MapStr{
			"bytes_long": memory.Stats.Swap,
		},
		"unevictable": common.MapStr{
			"bytes_long": memory.Stats.Unevictable,
		},
	}

	return memMap
}

// cgroupBlockIOToMapStr returns a MapStr containing BlockIOSubsystem data.
// If the blockIO parameter is nil then nil is returned.
func cgroupBlockIOToMapStr(blockIO *cgroup.BlockIOSubsystem) common.MapStr {
	if blockIO == nil {
		return nil
	}

	return common.MapStr{
		"id_string":   blockIO.ID,
		"path_string": blockIO.Path,
		"total": common.MapStr{
			"bytes_long": blockIO.Throttle.TotalBytes,
			"ios_long":   blockIO.Throttle.TotalIOs,
		},
	}
}
