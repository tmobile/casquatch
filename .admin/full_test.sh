#!/bin/sh
#
# Runs through tests of the full flow and project
#

CASSANDRA_VERSION=latest
DSE_VERSION=5.1.8

echo "---------------------------------------------"
echo "Starting Docker (30 second pause to startup)"
echo "---------------------------------------------"
docker run --rm  -p 9042:9042 -d --name installTest -d cassandra:$CASSANDRA_VERSION
sleep 30

echo "---------------------------------------------"
echo "Installing Keyspace"
echo "---------------------------------------------"
docker exec -i installTest cqlsh << EOF
CREATE KEYSPACE installTest WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;
CREATE TYPE installTest.test_udt (val1 text, val2 int);
CREATE TABLE installTest.test_udt_table (id uuid primary key, udt frozen<test_udt>);
CREATE TABLE installTest.table_name (key_one int,key_two int,col_one text,col_two text,PRIMARY KEY ((key_one, key_two)));
EOF

echo "---------------------------------------------"
echo "Configure project"
echo "---------------------------------------------"
mkdir config 2>/dev/null
cat << EOF > config/application.properties
cassandraDriver.contactPoints=localhost
cassandraDriver.localDC=datacenter1
cassandraDriver.keyspace=installTest
cassandraDriver.features.driverConfig=disabled
EOF

echo "---------------------------------------------"
echo "Run Driver Tests"
echo "---------------------------------------------"
mvn -pl cassandradriver -q test

echo "---------------------------------------------"
echo "Running install"
echo "---------------------------------------------"
./install.sh installTest

echo "---------------------------------------------"
echo "Installing Spring Keyspace"
echo "---------------------------------------------"
docker exec -i installTest cqlsh << EOF
CREATE KEYSPACE springconfig WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;
CREATE TABLE springconfig.configuration (application text,profile text,label text,key text,value text,PRIMARY KEY ((application, profile), label, key)) WITH CLUSTERING ORDER BY (label ASC, key ASC);
INSERT INTO springconfig.configuration (application,profile,label,key,value) values ('app','test','labelname','keyname','valuevalue');
EOF

echo "---------------------------------------------"
echo "Running install - Spring Config"
echo "---------------------------------------------"
./install.sh springconfig

echo "---------------------------------------------"
echo "Configure Spring Config"
echo "---------------------------------------------"
cat << EOF > config/application.properties
cassandraDriver.contactPoints=localhost
cassandraDriver.localDC=datacenter1
cassandraDriver.keyspace=springconfig
cassandraDriver.features.driverConfig=disabled
security.user.name=test
security.user.password=test
server.port=8888
EOF

echo "---------------------------------------------"
echo "Starting Spring Config"
echo "---------------------------------------------"
nohup mvn -pl springconfigserver spring-boot:run > nohup.out 2>&1 &
echo "Waiting for startup"
until grep -q "Started Application" nohup.out; do sleep 1; done;

echo "---------------------------------------------"
echo "Pulling test config. Should be keyname: valuevalue"
echo "---------------------------------------------"
curl -u test:test http://localhost:8888/app-test.yml
curl -u test:test http://localhost:8888/app/test/labelname

echo "---------------------------------------------"
echo "Cleanup"
echo "---------------------------------------------"
pkill -f -9 cassandragenerator > /dev/null 2>&1
pkill -f -9 springconfig > /dev/null 2>&1
docker stop installTest > /dev/null 2>&1
rm -rf config/application.properties

echo "---------------------------------------------"
echo "Starting DSE Tests"
echo "---------------------------------------------"

echo "---------------------------------------------"
echo "Starting Docker (60 second pause to startup)"
echo "---------------------------------------------"
docker run --rm -e DS_LICENSE=accept -p 9042:9042 -d --name installTestDSE -d datastax/dse-server:$DSE_VERSION -s
sleep 60

echo "---------------------------------------------"
echo "Installing Keyspace"
echo "---------------------------------------------"
docker exec -i installTestDSE cqlsh << EOF
CREATE KEYSPACE junitTest WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;
CREATE TABLE junitTest.table_name (key_one int,key_two int,col_one text,col_two text,PRIMARY KEY ((key_one, key_two)));
EOF

echo "---------------------------------------------"
echo "Create Core"
echo "---------------------------------------------"
docker exec -i installTestDSE bash << EOF
dsetool create_core junittest.table_name generateResources=true reindex=true
EOF

echo "---------------------------------------------"
echo "Run DSE Tests"
echo "---------------------------------------------"
mvn -pl cassandradriver -q -Dtest=CassandraDriverDSETests test

echo "---------------------------------------------"
echo "Cleanup"
echo "---------------------------------------------"
docker stop installTestDSE > /dev/null 2>&1
