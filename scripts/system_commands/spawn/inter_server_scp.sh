#!/bin/bash

# first commandline argument is the source server, the second is the destination server, the third is the file
cd $(dirname $0)
KEY=$(./get_key.sh)
scp -i $KEY dahayton@$1:lib/apache2/htdocs/$3 $3
scp -i $KEY $3 dahayton@$2:lib/apache2/htdocs/$3
