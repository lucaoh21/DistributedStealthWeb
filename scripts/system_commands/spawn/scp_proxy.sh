#!/bin/bash
# My first script
#first arg is host, second arg is keypair location
scp -i $2 -r ../../../src dahayton@$1:lib/proxy

