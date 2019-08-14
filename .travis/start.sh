#!/bin/bash

source .travis/env.sh

startNode() {
    IMAGE=$1
    NODENAME=$2
    SEED=$3

    log "Starting Node - $NODENAME "

    PARAMS="--network cassandra_test_default --label casquatch_test=$NODENAME"

    if [ ! -z "$SEED" ]; then
        SEED_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $SEED `
        if [[ "$IMAGE" == "cassandra"* ]]; then
            PARAMS="$PARAMS -e CASSANDRA_SEEDS=$SEED_IP"
        else
            PARAMS="$PARAMS -e SEEDS=$SEED_IP"
        fi;
    fi;

    if [[ "$IMAGE" == "cassandra"* ]]; then
        docker run --rm $PARAMS -e CASSANDRA_DC=$DATACENTER -e CASSANDRA_ENDPOINT_SNITCH=GossipingPropertyFileSnitch -d --name $NODENAME $IMAGE >>/dev/null
    else
        docker run --rm $PARAMS -e DS_LICENSE=accept -e DC=$DATACENTER -d --name $NODENAME $IMAGE >>/dev/null
    fi;
    sleep 5
    if [ $? -eq 0 ]; then
        retryLoop "docker exec -it $NODENAME nodetool status 2>/dev/null | egrep '^[A-Z]{2}' | egrep -c -v '^UN' | grep -q 0 " 300
        retryLoop "echo exit | docker exec -i $NODENAME cqlsh 2>/dev/null " 300
    else
        $?
    fi;
    showStatus $?
}

log "Starting Docker Cluster - $BASENAME "

if [ -z $2 ]; then
    NODES=$DEFAULT_NODE_COUNT
else
    NODES=$3
fi;

docker network create cassandra_test_default > /dev/null 2>&1

startNode $IMAGE ${BASENAME}

for (( n=2; n<=$NODES; n++)); do
    startNode $IMAGE ${BASENAME}_n${n} ${BASENAME}
done;

log "Installing Keyspace to $BASENAME"
docker exec -i $BASENAME cqlsh << EOF
  CREATE KEYSPACE IF NOT EXISTS $KEYSPACE WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;
  CREATE TABLE IF NOT EXISTS $KEYSPACE."table_name" ( "key_one" int, "key_two" int, "col_one" text, "col_two" text, PRIMARY KEY ("key_one", "key_two") );
EOF
showStatus $?

log "Configure driver"
mkdir config 2>>$OUTPUT
SEED_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $BASENAME `
cat << EOF > config/application.properties
casquatch.basic.contact-points.0=$SEED_IP:9042
casquatch.basic.load-balancing-policy.local-datacenter=$DATACENTER
casquatch.basic.session-keyspace=$KEYSPACE
casquatch.generator.createPackage=true
casquatch.generator.outputFolder=cassandramodels
casquatch.generator.file=true
casquatch.basic.request.timeout=10 seconds
casquatch.profiles.ddl.basic.request.timeout=10 seconds
advanced.reconnect-on-init=true
EOF
showStatus $?

log "Installing Casquatch"
$MVN clean install package
showStatus $?