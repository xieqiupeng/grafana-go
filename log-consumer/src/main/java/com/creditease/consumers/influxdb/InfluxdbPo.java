package com.creditease.consumers.influxdb;

import java.util.HashMap;
import java.util.Map;

public class InfluxdbPo {

    private String measurement;

    private Map<String, Object> fields = new HashMap<>();

    private Map<String, String> tags = new HashMap<>();

    private long createTime;

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
