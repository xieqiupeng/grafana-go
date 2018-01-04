package com.creditease.monitor.dataclean;

import java.util.List;

public class DataCleanRuleEntity {

    private String template;

    private Separator separator;

    private List<DataMapping> resultColumns;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Separator getSeparator() {
        return separator;
    }

    public void setSeparator(Separator separator) {
        this.separator = separator;
    }

    public List<DataMapping> getResultColumns() {
        return resultColumns;
    }

    public void setResultColumns(List<DataMapping> resultColumns) {
        this.resultColumns = resultColumns;
    }

    /**
     *分隔符内容
     */
    public static class Separator{

        private Boolean isOrder;

        private Boolean isRegex;

        private List<String> separatorKeys;

        public Boolean isOrder() {
            return isOrder;
        }

        public void setOrder(Boolean order) {
            isOrder = order;
        }

        public Boolean isRegex() {
            return isRegex;
        }

        public void setRegex(Boolean regex) {
            isRegex = regex;
        }

        public List<String> getSeparatorKeys() {
            return separatorKeys;
        }

        public void setSeparatorKeys(List<String> separatorKeys) {
            this.separatorKeys = separatorKeys;
        }
    }

    /**
     *  切割映射
     */
    public static class DataMapping{
        /**列名*/
        private String columnName;
        /**类型*/
        private String columnType;
        /**格式说明（时间类型）*/
        private String format;
        /**是tag还是fields*/
        private Integer tagOrValue;
        /**列下标*/
        private Integer columnSeq;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public Integer getTagOrValue() {
            return tagOrValue;
        }

        public void setTagOrValue(Integer tagOrValue) {
            this.tagOrValue = tagOrValue;
        }

        public Integer getColumnSeq() {
            return columnSeq;
        }

        public void setColumnSeq(Integer columnSeq) {
            this.columnSeq = columnSeq;
        }
    }
}
