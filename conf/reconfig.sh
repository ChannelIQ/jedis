#! /bin/bash
MASTER_NAME=$1
ROLE=$2
STATE=$3
FROM_IP=$4
FROM_PORT=$5
TO_IP=$6
TO_PORT=$7

echo "Sentinel Reconfigure ($MASTER_NAME): Setting role: ($ROLE) to state: ($STATE).  From ($FROM_IP:$FROM_PORT) to ($TO_IP:$TO_PORT)." >> /var/log/local-redis.log
