#!/bin/sh
#ip_address_list = /Users/DylanHR/desktop/dis-sys/ip-list
COUNT=0
for HOST in $(./get_ips.sh)
do
    echo $HOST
    ssh -i $(./get_keypair.sh)  dahayton@$HOST < start_proxy.sh 2>&1 | grep -v "Pseudo-terminal will not be allocated because\
 stdin is not a terminal." &
    pids[$COUNT]=$!
    COUNT=$COUNT+1
done

for pid in ${pids[@]}
do
    wait $pid
done
echo Proxies Started!
