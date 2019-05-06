#!/bin/sh
#starts proxy and host on the given ip $1
cd $(dirname $0)
HOST=$1
KEY=$(./get_keypair.sh)
ssh -i $KEY  dahayton@$HOST < start_apache.sh;
ssh -i $KEY -f dahayton@$HOST 'cd lib/proxy; java ProxyServer'
