#!/bin/bash
PORTS=$(seq 6380 1 6385)

#FURTHER TESTING: See if redis server can start when GraServer is online
#Also, implement node reconnectivity when they fail.
#TODO: Implement node reconnectivity to cluster

# set up servers
#rm -rf appendonlydir
#rm -f dump.rdb
for i in $PORTS
do
    rm -rf $i
    mkdir $i
    touch $i/redis.conf
    touch $i/nodes.conf
    echo "port $i
cluster-enabled yes
cluster-config-file $i/nodes.conf
cluster-node-timeout 5000
appendonly yes" >> $i/redis.conf
sleep 0.2
done
# set up individual redis
tmux new-session -s rc -d
for i in $PORTS
do
    tmux send-keys "redis-server ./$i/redis.conf" Enter
    tmux split-window -h
    tmux select-layout tiled
done
# set up redis cluster
CMD="redis-cli --cluster create"
for i in $PORTS; do CMD+=" 127.0.0.1:$i"; done
CMD+=" --cluster-replicas 1 --cluster-yes"
tmux send-keys "$CMD" Enter

# attach session
tmux -2 attach-session -d
