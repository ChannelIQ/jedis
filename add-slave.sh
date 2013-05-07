#! /bin/bash
PORT=$1
MASTER_PORT=$2

redis-cli -a foobared -p $PORT slaveof 127.0.0.1 $MASTER_PORT
redis-cli -a foobared -p $PORT config set masterauth foobared
