#!/bin/bash

export DATACENTER=dc1
export DATACENTER2=dc2
export KEYSPACE=fulltest
export OUTPUT=.admin/test.out
export VERSION=2.0-SNAPSHOT
export BASEDIR=`pwd`
export WORKER="docker run --rm --network cassandra_test_default -v $BASEDIR/maven_docker:/root/.m2 -v $BASEDIR:$BASEDIR -w $BASEDIR -it maven:latest"
export MVN="$WORKER mvn "
export JAVA="$WORKER java"
export DEFAULT_NODE_COUNT=1

export GENERATOR=casquatch-generator/target/casquatch-generator-$VERSION.jar

export IMAGE=$1
export BASENAME=`echo $IMAGE | tr -cd '[:alnum:]'`

export SEED_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $BASENAME `


export RED="\033[1;31m"
export GREEN="\033[1;32m"
export MAGENTA="\033[1;35m"
export BOLD="\033[1m"
export NOCOLOR="\033[0m"

export LOG_HEADER="[${MAGENTA}Casquatch Build${NOCOLOR}]"

#retryLoop command timeout
#Run the command until it succeeds or hits the timeout
retryLoop() {
  command=$1
  timeout=$2
  lc=0
  retcode=-1
  sleep=10
  until [ $retcode -eq 0 ]; do
    if [ $lc -gt $((timeout/sleep)) ]; then
      return 1
    fi;
    lc=$((lc+1))
    sleep $sleep;
    eval $command
    retcode=$?
  done;
  sleep $((sleep*2))
}

#runTests package test
#runs a speciifc junit test
runTests() {
  log "Run Tests $1:$2"
  $MVN -pl $1 -Dconfig.file=../config/application.properties -Dtest=$2 test
}

#showStatus returnCode
#Shows return code as a colored status
showStatus() {
  if [ $1 -eq 0 ]; then
    log "${GREEN}Success${NOCOLOR}"
  else
    log "${RED}Fail${NOCOLOR}"
    exit $1;
  fi
}

#log message
#Log a message
log() {
  echo -e "$LOG_HEADER ${BOLD}$1${NOCOLOR}"
}