#!/bin/bash

PORT=$(( ( RANDOM % 300 )  + 9000 ))
GENERATOR=../../casquatch-generator/target/casquatch-generator-2.0-SNAPSHOT.jar
DOCKER_VERSION=cassandra:latest
TEST_COMMAND="mvn clean test"
RUN_COMMAND="mvn clean spring-boot:run"

APP_LIST=(*/env);
APP_LIST_COUNT=${#APP_LIST[@]}

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
echo "Starting Docker (30 second pause to startup)"
echo "---------------------------------------------"
echo "Docker will be started with cassandra on 127.0.0.1:$PORT"
docker kill $NAME
docker run --rm  -p $PORT:9042 -d --label casquatch_example=$NAME --name $NAME -d cassandra:latest
sleep 30

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
