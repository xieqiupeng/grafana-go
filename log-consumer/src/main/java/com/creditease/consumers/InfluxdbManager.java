package com.creditease.consumers;

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

    private InfluxDB influxDB;
    private String dbName;

    private InfluxdbManager() {

        influxDB = InfluxDBFactory.connect("http://10.100.139.152:8086");
        dbName = "test";
        if (!influxDB.databaseExists(dbName)) {
            influxDB.createDatabase(dbName);
        }
    }

    public void insertPoint(List<Map<String, Object>> fields, String measurement) {

        BatchPoints batchPoints = BatchPoints.database(dbName)
                .tag("async", "true")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        //
        Point p = null;
        for (Map<String, Object> items : fields) {
            p = Point.measurement(measurement)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .fields(items)
                    .build();
            batchPoints.point(p);
        }
        influxDB.write(batchPoints);
    }


    public static InfluxdbManager getInstance() {

        return Singleton.INSTANCE.getInstance();
    }

    private static enum Singleton {

        INSTANCE;

        private InfluxdbManager influxdbManager;

        Singleton() {
            influxdbManager = new InfluxdbManager();
        }

        public InfluxdbManager getInstance() {
            return influxdbManager;
        }

    }

}
