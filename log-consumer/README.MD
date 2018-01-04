#启动方式
## nohup java -jar {jar包位置} {日志位置} {加载资源名称} >/dev/null &
例:nohup java -jar -server -Xmx1024M -Xms1024M log-consumer.jar /app/consumer application_prod.properties >/dev/null &