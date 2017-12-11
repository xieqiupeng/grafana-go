package com.creditease.consumers.dataclean;

import java.util.List;

public interface IDataClean {
    /**
     * 数据清洗
     * @param monitorName 数据源名称
     * @param monitorData 数据
     * @return
     */
    public List<DataCleanEntity> clean(String monitorName, String monitorData);
}
