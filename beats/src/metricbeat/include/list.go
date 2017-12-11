/*
Package include imports all Module and MetricSet packages so that they register
their factories with the global registry. This package can be imported in the
main package to automatically register all of the standard supported Metricbeat
modules.
*/
package include

import (
	// This list is automatically generated by `make imports`
	_ "metricbeat/module/aerospike"
	_ "metricbeat/module/aerospike/namespace"
	_ "metricbeat/module/apache"
	_ "metricbeat/module/apache/status"
	_ "metricbeat/module/ceph"
	_ "metricbeat/module/ceph/cluster_disk"
	_ "metricbeat/module/ceph/cluster_health"
	_ "metricbeat/module/ceph/cluster_status"
	_ "metricbeat/module/ceph/monitor_health"
	_ "metricbeat/module/ceph/pool_disk"
	_ "metricbeat/module/couchbase"
	_ "metricbeat/module/couchbase/bucket"
	_ "metricbeat/module/couchbase/cluster"
	_ "metricbeat/module/couchbase/node"
	_ "metricbeat/module/docker"
	_ "metricbeat/module/docker/container"
	_ "metricbeat/module/docker/cpu"
	_ "metricbeat/module/docker/diskio"
	_ "metricbeat/module/docker/healthcheck"
	_ "metricbeat/module/docker/image"
	_ "metricbeat/module/docker/info"
	_ "metricbeat/module/docker/memory"
	_ "metricbeat/module/docker/network"
	_ "metricbeat/module/dropwizard"
	_ "metricbeat/module/dropwizard/collector"
	_ "metricbeat/module/elasticsearch"
	_ "metricbeat/module/elasticsearch/node"
	_ "metricbeat/module/elasticsearch/node_stats"
	_ "metricbeat/module/etcd"
	_ "metricbeat/module/etcd/leader"
	_ "metricbeat/module/etcd/self"
	_ "metricbeat/module/etcd/store"
	_ "metricbeat/module/golang"
	_ "metricbeat/module/golang/expvar"
	_ "metricbeat/module/golang/heap"
	_ "metricbeat/module/graphite"
	_ "metricbeat/module/graphite/server"
	_ "metricbeat/module/haproxy"
	_ "metricbeat/module/haproxy/info"
	_ "metricbeat/module/haproxy/stat"
	_ "metricbeat/module/http"
	_ "metricbeat/module/http/json"
	_ "metricbeat/module/http/server"
	_ "metricbeat/module/jolokia"
	_ "metricbeat/module/jolokia/jmx"
	_ "metricbeat/module/kafka"
	_ "metricbeat/module/kafka/consumergroup"
	_ "metricbeat/module/kafka/partition"
	_ "metricbeat/module/kibana"
	_ "metricbeat/module/kibana/status"
	_ "metricbeat/module/kubernetes"
	_ "metricbeat/module/kubernetes/container"
	_ "metricbeat/module/kubernetes/event"
	_ "metricbeat/module/kubernetes/node"
	_ "metricbeat/module/kubernetes/pod"
	_ "metricbeat/module/kubernetes/state_container"
	_ "metricbeat/module/kubernetes/state_deployment"
	_ "metricbeat/module/kubernetes/state_node"
	_ "metricbeat/module/kubernetes/state_pod"
	_ "metricbeat/module/kubernetes/state_replicaset"
	_ "metricbeat/module/kubernetes/system"
	_ "metricbeat/module/kubernetes/util"
	_ "metricbeat/module/kubernetes/volume"
	_ "metricbeat/module/memcached"
	_ "metricbeat/module/memcached/stats"
	_ "metricbeat/module/mongodb"
	_ "metricbeat/module/mongodb/dbstats"
	_ "metricbeat/module/mongodb/status"
	_ "metricbeat/module/mysql"
	_ "metricbeat/module/mysql/status"
	_ "metricbeat/module/nginx"
	_ "metricbeat/module/nginx/stubstatus"
	_ "metricbeat/module/php_fpm"
	_ "metricbeat/module/php_fpm/pool"
	_ "metricbeat/module/postgresql"
	_ "metricbeat/module/postgresql/activity"
	_ "metricbeat/module/postgresql/bgwriter"
	_ "metricbeat/module/postgresql/database"
	_ "metricbeat/module/prometheus"
	_ "metricbeat/module/prometheus/collector"
	_ "metricbeat/module/prometheus/stats"
	_ "metricbeat/module/rabbitmq"
	_ "metricbeat/module/rabbitmq/node"
	_ "metricbeat/module/rabbitmq/queue"
	_ "metricbeat/module/redis"
	_ "metricbeat/module/redis/info"
	_ "metricbeat/module/redis/keyspace"
	_ "metricbeat/module/system"
	_ "metricbeat/module/system/core"
	_ "metricbeat/module/system/cpu"
	_ "metricbeat/module/system/diskio"
	_ "metricbeat/module/system/filesystem"
	_ "metricbeat/module/system/fsstat"
	_ "metricbeat/module/system/load"
	_ "metricbeat/module/system/memory"
	_ "metricbeat/module/system/network"
	_ "metricbeat/module/system/process"
	_ "metricbeat/module/system/process_summary"
	_ "metricbeat/module/system/socket"
	_ "metricbeat/module/system/uptime"
	_ "metricbeat/module/vsphere"
	_ "metricbeat/module/vsphere/datastore"
	_ "metricbeat/module/vsphere/host"
	_ "metricbeat/module/vsphere/virtualmachine"
	_ "metricbeat/module/windows"
	_ "metricbeat/module/windows/perfmon"
	_ "metricbeat/module/windows/service"
	_ "metricbeat/module/zookeeper"
	_ "metricbeat/module/zookeeper/mntr"
)
