package com.creditease.monitor.vo;

public class CutExampleVo {

    private String columnName;

    private String columnFormat;

    /**列下标*/
    private Integer columnSeq;

    /**类型*/
    private String columnType;

    /**是tag还是fields*/
    private Integer tagOrValue;

    private Object columnExampleValue;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnFormat() {
        return columnFormat;
    }

    public void setColumnFormat(String columnFormat) {
        this.columnFormat = columnFormat;
    }

    public Object getColumnExampleValue() {
        return columnExampleValue;
    }

    public void setColumnExampleValue(Object columnExampleValue) {
        this.columnExampleValue = columnExampleValue;
    }
    public Integer getColumnSeq() {
        return columnSeq;
    }

    public void setColumnSeq(Integer columnSeq) {
        this.columnSeq = columnSeq;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public Integer getTagOrValue() {
        return tagOrValue;
    }

    public void setTagOrValue(Integer tagOrValue) {
        this.tagOrValue = tagOrValue;
    }
}
