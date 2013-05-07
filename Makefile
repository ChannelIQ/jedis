define REDIS1_CONF
daemonize yes
port 6379
requirepass foobared
pidfile /tmp/redis1.pid
endef

define REDIS2_CONF
daemonize yes
port 6380
requirepass foobared
pidfile /tmp/redis2.pid
slaveof 127.0.0.1 6379
masterauth foobared
endef

define REDIS1_SENTINEL_MASTER2_CONF
daemonize yes
port 6479
requirepass foobared
pidfile /tmp/redis3.pid
endef

define REDIS2_SENTINEL_MASTER2_CONF
daemonize yes
port 6480
requirepass foobared
pidfile /tmp/redis4.pid
slaveof 127.0.0.1 6479
masterauth foobared
endef

define REDIS_SENTINEL1
port 26379
daemonize yes
sentinel monitor mymaster 127.0.0.1 6379 2
sentinel auth-pass mymaster foobared
sentinel down-after-milliseconds mymaster 1000
sentinel failover-timeout mymaster 900000
sentinel can-failover mymaster yes
sentinel parallel-syncs mymaster 1

sentinel monitor mymaster2 127.0.0.1 6479 2
sentinel auth-pass mymaster2 foobared
sentinel down-after-milliseconds mymaster2 1000
sentinel failover-timeout mymaster2 900000
sentinel can-failover mymaster2 yes
sentinel parallel-syncs mymaster2 1
pidfile /tmp/sentinel1.pid
endef

define REDIS_SENTINEL2
port 26380
daemonize yes
sentinel monitor mymaster 127.0.0.1 6379 2
sentinel auth-pass mymaster foobared
sentinel down-after-milliseconds mymaster 1000
sentinel failover-timeout mymaster 900000
sentinel can-failover mymaster yes
sentinel parallel-syncs mymaster 1

sentinel monitor mymaster2 127.0.0.1 6479 2
sentinel auth-pass mymaster2 foobared
sentinel down-after-milliseconds mymaster2 1000
sentinel failover-timeout mymaster2 900000
sentinel can-failover mymaster2 yes
sentinel parallel-syncs mymaster2 1
pidfile /tmp/sentinel2.pid
endef

export REDIS1_CONF
export REDIS2_CONF
export REDIS_SENTINEL1
export REDIS_SENTINEL2
export REDIS1_SENTINEL_MASTER2_CONF
export REDIS2_SENTINEL_MASTER2_CONF
test:
	echo "$$REDIS1_CONF" | redis-server -
	echo "$$REDIS2_CONF" | redis-server -
	echo "$$REDIS_SENTINEL1" | redis-sentinel -
	echo "$$REDIS_SENTINEL2" | redis-sentinel -
	echo "$$REDIS1_SENTINEL_MASTER2_CONF" | redis-server -
	echo "$$REDIS2_SENTINEL_MASTER2_CONF" | redis-server -

	mvn clean compile test

	kill `cat /tmp/redis1.pid`
	kill `cat /tmp/redis2.pid`
	kill `cat /tmp/redis3.pid`
	kill `cat /tmp/redis4.pid`
	kill `cat /tmp/sentinel1.pid`
	kill `cat /tmp/sentinel2.pid`

.PHONY: tests
