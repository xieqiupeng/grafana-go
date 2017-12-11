package com.creditease.consumers.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by creditease on 17/11/30.
 */
public class InfluxdbManager {

    private static final String protocol = "http://";
    private InfluxDB influxDB;
    private String database;
    public InfluxdbManager(String address,String database) {
        influxDB = InfluxDBFactory.connect(protocol.concat(address));
        if(!influxDB.databaseExists(database)){
            influxDB.createDatabase(database);
        }
        this.database = database;
    }

    public void insertPoint(List<InfluxdbPo> influxdbPo){
        BatchPoints batchPoints = BatchPoints.database(database).tag("async","true").consistency(InfluxDB.ConsistencyLevel.ALL).build();
        influxdbPo.forEach(po->{
            Point p = Point.measurement(po.getMeasurement()).time(po.getCreateTime(), TimeUnit.MILLISECONDS).tag(po.getTags()).fields(po.getFields()).build();
            batchPoints.point(p);
        });
        influxDB.write(batchPoints);
    }

    public void insertPoint(InfluxdbPo influxdbPo){
        BatchPoints batchPoints = BatchPoints.database(database).tag("async","true").consistency(InfluxDB.ConsistencyLevel.ALL).build();
        Point p = Point.measurement(influxdbPo.getMeasurement()).time(influxdbPo.getCreateTime(), TimeUnit.MILLISECONDS).tag(influxdbPo.getTags()).fields(influxdbPo.getFields()).build();
        batchPoints.point(p);
        influxDB.write(batchPoints);
    }

}
