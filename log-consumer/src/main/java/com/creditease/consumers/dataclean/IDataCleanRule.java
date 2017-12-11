package com.creditease.consumers.dataclean;

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
    public List<DataCleanEntity> clean(String monitorData);
}
