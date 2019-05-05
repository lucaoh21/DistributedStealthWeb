#!/bin/bash
FILE='dist-index.txt'
while read LINE
do
    ./upload.sh $LINE
done < $FILE
echo 'files uploaded!'