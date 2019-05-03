#!/bin/sh
#ip_address_list = /Users/DylanHR/desktop/dis-sys/ip-list
COUNT=0
for HOST in $(cat ../system_config/ip-list.txt)
do
    ssh -i ../system_config/dahayton-keypair dahayton@$HOST < stop_apache.sh 2>&1 | grep -v "Pseudo-terminal will not be allocated because\
 stdin is not a terminal." &
    pids[$COUNT]=$!
    COUNT=$COUNT+1
done

for pid in ${pids[@]}
do
    wait $pid
done
echo Servers Stopped!