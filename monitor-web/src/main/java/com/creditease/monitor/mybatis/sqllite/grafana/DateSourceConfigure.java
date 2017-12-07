package com.creditease.monitor.mybatis.sqllite.grafana;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DateSourceConfigure {
    @Value("${jdbc.default.driverClassName}")
    private String driverClassName;
    @Value("${jdbc.default.maxTotal}")
    private int maxTotal;
    @Value("${jdbc.default.initialSize}")
    private int initialSize;
    @Value("${jdbc.default.maxIdle}")
    private int maxIdle;
    @Value("${jdbc.default.maxWaitMillis}")
    private long maxWaitMillis;
    @Value("${jdbc.default.minIdle}")
    private int minIdle;
    @Value("${jdbc.default.removeAbandoned}")
    private boolean removeAbandoned;
    @Value("${jdbc.default.removeAbandonedTimeout}")
    private int removeAbandonedTimeout;
    @Value("${jdbc.default.numTestsPerEvictionRun}")
    private int numTestsPerEvictionRun;
    @Value("${jdbc.default.minEvictableIdleTimeMillis}")
    private long minEvictableIdleTimeMillis;
    @Value("${jdbc.default.timeBetweenEvictionRunsMillis}")
    private long timeBetweenEvictionRunsMillis;
    @Value("${jdbc.default.testWhileIdle}")
    private boolean testWhileIdle;
    @Value("${jdbc.default.validationQuery}")
    private String validationQuery;
    @Value("${jdbc.default.validationQueryTimeout}")
    private int validationQueryTimeout;

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

    public int getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

}
