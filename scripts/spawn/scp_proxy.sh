#!/bin/bash
# My first script

scp -i dahayton-keypair -r proxy dahayton@$1:lib/proxy/
