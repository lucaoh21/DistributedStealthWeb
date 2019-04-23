#!/bin/sh

tar xvjf httpd-2.4.39.tar.bz2
tar xvf apr-1.7.0.tar -C httpd-2.4.39/srclib 
tar xvf apr-util-1.6.1.tar -C httpd-2.4.39/srclib 
mv httpd-2.4.39/srclib/apr-util-1.6.1 httpd-2.4.39/srclib/apr-util
mv httpd-2.4.39/srclib/apr-1.7.0/ httpd-2.4.39/srclib/apr
cd httpd-2.4.39/
./configure --prefix=/home/dahayton/lib/apache2 --with-included-apr --with-port=8505
make
make install