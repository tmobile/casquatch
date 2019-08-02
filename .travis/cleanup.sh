#!/bin/bash

source .travis/env.sh

log "Cleanup"

docker ps -flabel=casquatch_test -q | xargs -I % docker kill % >>/dev/null 2>&1
docker container ls -a -flabel=casquatch_test -q | xargs -I % docker rm -f % >>/dev/null 2>&1
$MVN -q clean
rm -rf cassandramodels
rm -rf config/application.properties
rm -rf $HOME/maven_docker/repository/com/tmobile/opensource/casquatch