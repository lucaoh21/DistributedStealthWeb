#!/bin/bash
# My first script

scp -i dahayton-keypair -r apache_src/httpd-2.4.39.tar.bz2 dahayton@$1:
scp -i dahayton-keypair -r apache_src/apr-util-1.6.1.tar.gz dahayton@$1:
scp -i dahayton-keypair -r apache_src/apr-1.7.0.tar.gz dahayton@$1:

