#!/bin/bash

# provide ip as first command line arg
cd $(dirname $0)
KEY=$(./get_key.sh)
./scp_apache.sh $1 $KEY
ssh -i $KEY dahayton@$1 < ./build_apache.sh 
./scp_proxy.sh $1 $KEY