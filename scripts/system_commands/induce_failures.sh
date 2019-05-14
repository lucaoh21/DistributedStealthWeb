#!/bin/bash
COUNT=0
for HOST in $(./get_ips.sh)
do
    #A=$(($RANDOM % 100))
    #B=$((98-$1))
    if [ $1 -gt $COUNT ]
	then
	echo Shutting down $HOST
	ssh -i $(./get_keypair.sh) dahayton@$HOST < stop_apache.sh 2>&1 | grep -v "Pseudo-terminal will not be allocated because stdin is not a terminal."
    fi
    COUNT=$((B+1))
done
