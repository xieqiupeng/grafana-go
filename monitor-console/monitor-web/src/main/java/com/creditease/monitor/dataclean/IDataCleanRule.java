package com.creditease.monitor.dataclean;

import com.creditease.monitor.vo.CutExampleVo;

import java.util.List;

/**
 *
 */
public interface IDataCleanRule {
    /**
     *
     * @param monitorData 数据源
     * @return
     */
    public List<CutExampleVo> clean(String monitorData);
}
