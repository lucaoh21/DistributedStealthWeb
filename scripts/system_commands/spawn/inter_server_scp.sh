#!/bin/bash

# first commandline argument is the source server, the second is the destination server, the third is the file
scp -i dahayton-keypair dahayton@$1:lib/apache2/htdocs/$3 $3
scp -i dahayton-keypair $3 dahayton@$2:lib/apache2/htdocs/$3