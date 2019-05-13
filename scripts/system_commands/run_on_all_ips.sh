#!/bin/sh
#ip_address_list = /Users/DylanHR/desktop/dis-sys/ip-list
#takes one command line arg that specifies the comand to run on all hosts. Pass a .sh file.
COUNT=0
for HOST in $(cat ../../system_config/ip-list.txt)
do
    ssh -i $(./get_keypair.sh)  dahayton@$HOST < $1 2>&1 | grep -v "Pseudo-terminal will not be allocated because\
 stdin is not a terminal." &
    pids[$COUNT]=$!
    COUNT=$COUNT+1
done

for pid in ${pids[@]}
do
    wait $pid
done
echo executed on all Servers!
