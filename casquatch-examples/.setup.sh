#!/bin/bash

PORT=$(( ( RANDOM % 300 )  + 9000 ))
GENERATOR=../../casquatch-generator/target/casquatch-generator-2.0-RELEASE.jar
DOCKER_VERSION=cassandra:latest
TEST_COMMAND="mvn clean test"
RUN_COMMAND="mvn clean spring-boot:run"

APP_LIST=(*/env);
APP_LIST_COUNT=${#APP_LIST[@]}

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

i=1
if [ -z $1 ]; then
    for app in "${APP_LIST[@]}"; do
        echo "$i) "`echo $app | rev | cut -c 5- | rev`
        i=$(( i + 1 ))
    done;
    echo -n "Please select an app to run: "; read appnum;
else
    for app in "${APP_LIST[@]}"; do
        if [ "$1" == "`echo $app | rev | cut -c 5- | rev`" ]; then
            echo "Found Match"
            echo `echo $app | rev | cut -c 5- | rev`
            appnum=$i;
        fi;
        i=$(( i + 1 ))
    done;
fi;

source ${APP_LIST[appnum-1]}

cd $NAME

echo "---------------------------------------------"
echo "Starting Docker"
echo "---------------------------------------------"
echo "Docker will be started with cassandra on 127.0.0.1:$PORT"
docker kill $NAME
docker run --rm  -p $PORT:9042 -d --label casquatch_example=$NAME --name $NAME -d cassandra:latest
retryLoop "echo exit | docker exec -i $NAME cqlsh 2>/dev/null " 300

echo "---------------------------------------------"
echo "Installing CQL"
echo "---------------------------------------------"
cat $SCHEMA | docker exec -i $NAME cqlsh

echo "---------------------------------------------"
echo "Configure project"
echo "---------------------------------------------"
sed -i .old "s/127.0.0.1:[0-9]\{4\}/127.0.0.1:$PORT/" src/main/resources/application.conf
rm src/main/resources/application.conf.old

echo "---------------------------------------------"
echo "Generate Entity"
echo "---------------------------------------------"
java -Dconfig.file=src/main/resources/application.conf -jar $GENERATOR
mkdir -p src/test/java/com/tmobile/opensource/casquatch/examples/$NAME/
mv src/main/java/com/tmobile/opensource/casquatch/examples/$NAME/*EmbeddedTests.java src/test/java/com/tmobile/opensource/casquatch/examples/$NAME/
