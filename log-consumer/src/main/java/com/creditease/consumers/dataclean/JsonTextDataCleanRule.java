package com.creditease.consumers.dataclean;

import java.util.List;

public class JsonTextDataCleanRule implements IDataCleanRule {

    private DataCleanRuleEntity dataCleanRuleEntity;

    public JsonTextDataCleanRule(DataCleanRuleEntity dataCleanRuleEntity) {
        this.dataCleanRuleEntity = dataCleanRuleEntity;
    }

    @Override
    public List<DataCleanEntity> clean(String monitorData) {
        throw new NullPointerException("暂不支持");
    }
}
