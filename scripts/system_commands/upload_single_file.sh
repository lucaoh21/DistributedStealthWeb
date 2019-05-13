#!/bin/bash

for HOST in $(./get_ips.sh)
do
    scp -i $(./get_keypair.sh) -r $1 dahayton@$HOST:lib/proxy
done

