package com.creditease.monitor.mybatis.sqllite.grafana.config;

import com.creditease.monitor.mybatis.sqllite.grafana.DateSourceConfigure;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.sqlite.JDBC;


import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages="com.creditease.monitor.mybatis.sqllite.grafana.mapper",sqlSessionFactoryRef="grafanaSqlSessionFactory")
public class GrafanaDataSourceConfig {
    @Autowired
    private DateSourceConfigure dateSourceConfigure;
    @Value("${jdbc.grafana.url}")
    private String url;
    @Value("${jdbc.grafana.username}")
    private String userName;
    @Value("${jdbc.grafana.password}")
    private String password;

    @Bean(name="grafanaDataSource",destroyMethod="close")
    public BasicDataSource createDataSource(){
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dateSourceConfigure.getDriverClassName());
//        ds.setUrl(JDBC.PREFIX.concat(url));
        ds.setUrl(url);
        ds.setUsername(userName);
        ds.setPassword(password);
        ds.setMaxTotal(dateSourceConfigure.getMaxTotal());
        ds.setInitialSize(dateSourceConfigure.getInitialSize());
        ds.setMaxIdle(dateSourceConfigure.getMaxIdle());
        ds.setMaxWaitMillis(dateSourceConfigure.getMaxWaitMillis());
        ds.setMinIdle(dateSourceConfigure.getMinIdle());
        ds.setRemoveAbandonedOnBorrow(dateSourceConfigure.isRemoveAbandoned());
        ds.setRemoveAbandonedOnMaintenance(dateSourceConfigure.isRemoveAbandoned());
        ds.setRemoveAbandonedTimeout(dateSourceConfigure.getRemoveAbandonedTimeout());
        ds.setTimeBetweenEvictionRunsMillis(dateSourceConfigure.getTimeBetweenEvictionRunsMillis());
        ds.setNumTestsPerEvictionRun(dateSourceConfigure.getNumTestsPerEvictionRun());
        ds.setMinEvictableIdleTimeMillis(dateSourceConfigure.getMinEvictableIdleTimeMillis());
        ds.setTestWhileIdle(dateSourceConfigure.isTestWhileIdle());
        ds.setValidationQuery(dateSourceConfigure.getValidationQuery());
        ds.setValidationQueryTimeout(dateSourceConfigure.getValidationQueryTimeout());
        return ds;
    }

    /**
     *
     * @Description: 生成SessionFactory
     * @param dataSource
     */
    @Bean(name = "grafanaSqlSessionFactory")
    public SqlSessionFactory createSqlSessionFactory(@Qualifier("grafanaDataSource") DataSource dataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX.concat("mybatis-conf.xml"));
        sessionFactory.setConfigLocation(resource);
        return sessionFactory.getObject();
    }
    /**
     *
     * @Description: TODO
     *
     * @param dataSource
     * @return DataSourceTransactionManager
     */
    @Bean(name="grafanaDataSourceTransactionManager")
    public DataSourceTransactionManager createDataSourceTransactionManager(@Qualifier("grafanaDataSource") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }
}
