package com.creditease.consumers.dataclean;

/**
 * 清洗数据实体
 */
public class DataCleanEntity {
    /**名称*/
    private String name;
    /**值*/
    private Object value;
    /**是否是索引数据*/
    private boolean isIndex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isIndex() {
        return isIndex;
    }

    public void setIndex(boolean index) {
        isIndex = index;
    }
}
