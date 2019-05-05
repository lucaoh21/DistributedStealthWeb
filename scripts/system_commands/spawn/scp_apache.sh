#!/bin/bash
# My first script
# first arg is host, second arg is keypair location
scp -i $2 -r apache_src/httpd-2.4.39.tar.bz2 dahayton@$1:
scp -i $2 -r apache_src/apr-util-1.6.1.tar.gz dahayton@$1:
scp -i $2 -r apache_src/apr-1.7.0.tar.gz dahayton@$1:

