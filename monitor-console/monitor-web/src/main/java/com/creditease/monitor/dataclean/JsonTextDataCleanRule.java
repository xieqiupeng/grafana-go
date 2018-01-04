package com.creditease.monitor.dataclean;

import com.creditease.monitor.vo.CutExampleVo;

import java.util.List;

public class JsonTextDataCleanRule implements IDataCleanRule {

    private DataCleanRuleEntity dataCleanRuleEntity;

    public JsonTextDataCleanRule(DataCleanRuleEntity dataCleanRuleEntity) {
        this.dataCleanRuleEntity = dataCleanRuleEntity;
    }

    @Override
    public List<CutExampleVo> clean(String monitorData) {
        throw new NullPointerException("暂不支持");
    }
}
