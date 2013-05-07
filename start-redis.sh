redis-server ./conf/mymaster.conf
redis-server ./conf/mymaster2.conf
redis-server ./conf/mymaster-slave.conf
redis-server ./conf/mymaster2-slave.conf
redis-sentinel ./conf/sentinel1.conf
redis-sentinel ./conf/sentinel2.conf
redis-sentinel ./conf/sentinel3.conf
