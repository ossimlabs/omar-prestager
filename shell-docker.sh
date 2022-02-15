#!/bin/sh

IMAGE_NAME=nexus-docker-private-hosted.ossim.io/omar-prestager
IMAGE_VERSION=latest
PORT_NUMBER=8888

docker run -it --rm  \
  -u root \
  -p $PORT_NUMBER:8080 \
  -v $OSSIM_DATA:$OSSIM_DATA \
  -v $OSSIM_DATA:/data \
  --entrypoint sh \
  $IMAGE_NAME:$IMAGE_VERSION



