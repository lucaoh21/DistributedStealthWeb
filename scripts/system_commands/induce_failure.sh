#!/bin/sh
ssh -i $(./get_keypair.sh) dahayton@$1 < stop_apache.sh 
