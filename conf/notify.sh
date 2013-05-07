#! /bin/bash
EVENT_TYPE=$1
EVENT_DESC=$2

echo "Sentinel: ($EVENT_TYPE) - $EVENT_DESC" >> /var/log/local-redis.log
