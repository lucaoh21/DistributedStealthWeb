#!/bin/bash
# My first script

scp -i dahayton-keypair -r httpd-2.4.39.tar.bz2 dahayton@$1:
scp -i dahayton-keypair -r apr-util-1.6.1.tar dahayton@$1:
scp -i dahayton-keypair -r apr-1.7.0.tar dahayton@$1:

