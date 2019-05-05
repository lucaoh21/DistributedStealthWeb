#!/bin/sh
#ip_address_list = /Users/DylanHR/desktop/dis-sys/ip-list
#takes one command line arg that specifies the comand to run on all hosts. Pass a .sh file.
COUNT=0
for HOST in $(cat ../../system_config/starting-ip-list.txt)
do
    echo $HOST
    spawn/spawn_node.sh $HOST &
    pids[$COUNT]=$!
    COUNT=$COUNT+1
done

for pid in ${pids[@]}
do
    wait $pid
done
echo Starting Servers Spawned!
