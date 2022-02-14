#!/bin/sh

IMAGE_DIR=$OSSIM_DATA/skysat-test/stitched
URL=http://localhost:8888

for x in `find $IMAGE_DIR -name "*.ntf" -o -name "*.tif"`; do
    curl -X POST "$URL/prestage/queueFile?filename=$x"
    echo "";
done