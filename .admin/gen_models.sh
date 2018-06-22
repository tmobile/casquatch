#!/bin/sh
#
# Helper script to spin up docker, load a model from cql and then generate the jars
#

KEYSPACE=$1
INSTANCE=genmodels

echo "---------------------------------------------"
echo "Starting Docker (30 second pause to startup)"
echo "---------------------------------------------"
docker run --rm  -p 9042:9042 -d --name $INSTANCE -d cassandra:latest
sleep 30

echo "---------------------------------------------"
echo "Installing CQL"
echo "---------------------------------------------"
cat ${KEYSPACE}.cql | docker exec -i $INSTANCE cqlsh

echo "---------------------------------------------"
echo "Configure project"
echo "---------------------------------------------"
mkdir config 2>/dev/null
cat << EOF > config/application.properties
cassandraDriver.contactPoints=localhost
cassandraDriver.localDC=datacenter1
cassandraDriver.keyspace=$KEYSPACE
cassandraDriver.features.driverConfig=disabled
EOF

echo "---------------------------------------------"
echo "Running install"
echo "---------------------------------------------"
./install.sh $KEYSPACE

echo "---------------------------------------------"
echo "Cleanup"
echo "---------------------------------------------"
docker stop $INSTANCE
rm config/application.properties
