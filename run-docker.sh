docker run -it --rm  -p 8888:8080 -v $OSSIM_DATA:$OSSIM_DATA -v $OSSIM_DATA:/data nexus-docker-public-hosted.ossim.io/omar-prestager:latest
#docker run -it --rm  -p 8888:8080 -e JAVA_OPTIONS="-Dmicronaut.server.context-path=/omar-prestager" -v $OSSIM_DATA:$OSSIM_DATA -v $OSSIM_DATA:/data nexus-docker-public-hosted.ossim.io/omar-prestager:latest
