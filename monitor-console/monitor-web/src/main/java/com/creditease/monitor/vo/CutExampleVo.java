package com.creditease.monitor.vo;

public class CutExampleVo {

    private String columnName;

    private String columnFormat;

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
}
