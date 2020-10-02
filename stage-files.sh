#!/bin/sh
for x in `find /System/Volumes/Data/ossim/epcot/*.NTF`; do
    curl -X POST "http://localhost:8080/prestage/queueFile?filename=$x"
    echo "";
done