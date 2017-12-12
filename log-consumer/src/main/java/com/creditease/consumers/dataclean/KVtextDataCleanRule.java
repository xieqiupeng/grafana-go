package com.creditease.consumers.dataclean;

import java.util.List;

public class KVtextDataCleanRule implements IDataCleanRule {

    private DataCleanRuleEntity dataCleanRuleEntity;

    public KVtextDataCleanRule(DataCleanRuleEntity dataCleanRuleEntity) {
        this.dataCleanRuleEntity = dataCleanRuleEntity;
    }

    @Override
    public List<DataCleanEntity> clean(String monitorData) {
       throw new NullPointerException("暂不支持");
    }
}
