#!/bin/sh
#
# Helper script to spin up docker and regenerate test models
#

VERSION=2.0-RELEASE
KEYSPACE=junittest
INSTANCE=casquatch-driver-tests
OUTPUT_FOLDER=src/test/java/com/tmobile/opensource/casquatch/tests/
GENERATOR=../casquatch-generator/target/casquatch-generator-$VERSION.jar
PACKAGE=com.tmobile.opensource.casquatch.tests
PORT=9044

echo "---------------------------------------------"
echo "Starting Docker (30 second pause to startup)"
echo "---------------------------------------------"
docker kill $INSTANCE
docker run --rm  -p $PORT:9042 -d --name $INSTANCE -d cassandra:latest
sleep 30

echo "---------------------------------------------"
echo "Installing CQL"
echo "---------------------------------------------"
docker exec -i $INSTANCE cqlsh <<EOF
CREATE KEYSPACE $KEYSPACE WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE $KEYSPACE.simple_table (
    key_one int,
    key_two int,
    col_one text,
    col_two text,
    solr_query text,
    PRIMARY KEY (key_one, key_two)
) WITH CLUSTERING ORDER BY (key_two ASC);

CREATE TYPE $KEYSPACE.udt ( val1 text, val2 text );

CREATE TABLE $KEYSPACE.torture_table (
    id uuid PRIMARY KEY,
    col_ascii ascii,
    col_bigint bigint,
    col_blob blob,
    col_boolean boolean,
    col_date date,
    col_decimal decimal,
    col_double double,
    col_float float,
    col_inet inet,
    col_int int,
    col_list list<text>,
    col_map map<text, text>,
    col_set set<text>,
    col_smallint smallint,
    col_text text,
    col_time time,
    col_timestamp timestamp,
    col_timeuuid timeuuid,
    col_tinyint tinyint,
    col_uuid uuid,
    col_varchar text,
    col_varint varint,
    col_udt frozen<udt>
);

CREATE TABLE $KEYSPACE.counter_table (
    id uuid PRIMARY KEY,
    col_counter counter
);

CREATE TABLE $KEYSPACE.tuple_table (
    id uuid PRIMARY KEY,
    col_tuple tuple<text,text,text>
);

EOF

echo "---------------------------------------------"
echo "Generate Models"
echo "---------------------------------------------"
java    -Dcasquatch.basic.contact-points.0=localhost:$PORT \
        -Dcasquatch.basic.load-balancing-policy.local-datacenter=datacenter1 \
        -Dcasquatch.basic.session-keyspace=$KEYSPACE \
        -Dcasquatch.generator.outputFolder=$OUTPUT_FOLDER \
        -Dcasquatch.generator.overwrite=true \
        -Dcasquatch.generator.file=true \
        -Dcasquatch.generator.packageName=$PACKAGE \
        -DCASQUATCH_LOG_LEVEL=TRACE \
        -jar $GENERATOR

echo "---------------------------------------------"
echo "Modify Output"
echo "---------------------------------------------"
rm $OUTPUT_FOLDER/*_EmbeddedTests.java
sed -i .bak 's/^\@CasquatchEntity\(\)$/\@CasquatchEntity\(generateTests = true\)/' $OUTPUT_FOLDER/*.java
rm $OUTPUT_FOLDER/*.java.bak


echo "---------------------------------------------"
echo "Kill Docker"
echo "---------------------------------------------"
docker kill $INSTANCE
