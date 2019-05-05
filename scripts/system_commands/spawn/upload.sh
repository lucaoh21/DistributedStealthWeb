#!/bin/bash
echo $1'-->'$2
scp -i dahayton-keypair lib/apache2/htdocs/$1 dahayton@$2:lib/apache2/htdocs
