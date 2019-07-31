#!/bin/sh
#
# Runs through tests of the full flow and project
#
DATACENTER=dc1
DATACENTER2=dc2
KEYSPACE=fulltest
OUTPUT=.admin/test.out
VERSION=2.0-SNAPSHOT
WORKER="docker run --rm --network cassandra_test_default -v $HOME/.m2:/root/.m2 -v `pwd`:`pwd` -w `pwd` -it maven:latest"
MVN="$WORKER mvn"
JAVA="$WORKER java"

#startCluster image name nodes
#Start up a cassandra cluster with the given parameters. Nodes defaults to 1
startCluster() {
  echo "---------------------------------------------"
  echo "Starting Docker Cluster - $1 "
  echo "---------------------------------------------"
  IMAGE=$1
  BASENAME=$2

  if [ -z $3 ]; then
    NODES=1
  else
    NODES=$3
  fi;

  startNode $IMAGE ${BASENAME}

    for (( n=2; n<=$NODES; n++)); do
        startNode $IMAGE ${BASENAME}_n${n} ${BASENAME}
    done;
}

startNode() {
    IMAGE=$1
    NODENAME=$2
    SEED=$3


    PARAMS="--network cassandra_test_default --label casquatch_test=$NODENAME"

    if [[ "$IMAGE" == "cassandra"* ]]; then
        PARAMS="$PARAMS -e CASSANDRA_DC=$DATACENTER -e CASSANDRA_ENDPOINT_SNITCH=GossipingPropertyFileSnitch "
    else
        PARAMS="$PARAMS -e DS_LICENSE=accept -e DC=$DATACENTER"
    fi

    if [ ! -z "$SEED" ]; then
        SEED_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $3 `
        if [[ "$IMAGE" == "cassandra"* ]]; then
            PARAMS="$PARAMS -e CASSANDRA_SEEDS=$SEED_IP"
        else
            PARAMS="$PARAMS -e SEEDS=$SEED_IP"
        fi;
    fi;

    echo "Starting node : docker run $PARAMS -d --name $NODENAME $IMAGE"
    docker run --rm -e JVM_EXTRA_OPTS='-Xms2g -Xmx2g' $PARAMS -d --name $NODENAME $IMAGE
    sleep 5
    if [ $? -eq 0 ]; then
        retryLoop "docker exec -it $NODENAME nodetool status 2>/dev/null | egrep '^[A-Z]{2}' | egrep -c -v '^UN' | grep -q 0 " 300
        retryLoop "echo exit | docker exec -i $NODENAME cqlsh 2>/dev/null " 300
    else
        $?
    fi;
# TODO
#    if [[ "$IMAGE" == "datastax"* ]]; then
#        configureSSL $NODENAME `echo $IMAGE | cut -c 21-`
#    fi
}

configureSSL() {
  echo "---------------------------------------------"
  echo "Configure SSL"
  echo "---------------------------------------------"
  export TRUSTSTORE_PATH=`pwd`/.admin/dsessl/.truststore
  docker cp .admin/dsessl/.keystore $1:/opt/dse/resources/dse/conf/.keystore
  docker cp .admin/dsessl/.truststore $1:/opt/dse/resources/dse/conf/.truststore
  docker cp .admin/dsessl/cassandra.yaml.`echo $2 | awk -F. '{print $1"."$2}'` $1:/opt/dse/resources/cassandra/conf/cassandra.yaml
  cp .admin/dsessl/client.truststore config/client.truststore
  docker restart $1
  retryLoop "docker exec -it $1 dsetool status 2>/dev/null | grep rack1 | grep -q 'UN'" 120

}

#installKeyspace name
#install the base keyspace
installKeyspace() {
  echo "---------------------------------------------"
  echo "Installing Keyspace"
  echo "---------------------------------------------"
  docker exec -i $1 cqlsh << EOF
CREATE KEYSPACE IF NOT EXISTS $KEYSPACE WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;
EOF
}

installTables() {
  installKeyspace $1
  echo "---------------------------------------------"
  echo "Installing Tables"
  echo "---------------------------------------------"
  docker exec -i $1 cqlsh << EOF
  use $KEYSPACE;
  CREATE TABLE IF NOT EXISTS "table_name" ( "key_one" int, "key_two" int, "col_one" text, "col_two" text, PRIMARY KEY ("key_one", "key_two") );
EOF
}
#installSpringKeyspace name
#install the spring keyspace
installSpringKeyspace() {
  echo "---------------------------------------------"
  echo "Installing Spring Keyspace"
  echo "---------------------------------------------"
  docker exec -i $1 cqlsh << EOF
CREATE KEYSPACE IF NOT EXISTS springconfig WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;
CREATE TABLE IF NOT EXISTS springconfig.configuration (application text,profile text,label text,key text,value text,PRIMARY KEY ((application, profile), label, key)) WITH CLUSTERING ORDER BY (label ASC, key ASC);
INSERT INTO springconfig.configuration (application,profile,label,key,value) values ('app','test','labelname','keyname','valuevalue');
EOF
}

#configureDriver keyspace
#Configure casquatch driver for the keyspace
configureDriver() {
  echo "---------------------------------------------"
  echo "Configure driver"
  echo "---------------------------------------------"
  mkdir config 2>>$OUTPUT
  SEED_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $1 `
cat << EOF > config/application.properties
casquatch.basic.contact-points.0=$SEED_IP:9042
casquatch.basic.load-balancing-policy.local-datacenter=$DATACENTER
casquatch.basic.session-keyspace=$2
casquatch.generator.createPackage=true
casquatch.generator.outputFolder=cassandramodels
casquatch.generator.file=true
casquatch.basic.request.timeout=1 second
advanced.reconnect-on-init=true

javax.net.ssl.trustStorePassword=cassandra
javax.net.ssl.trustStore=$TRUSTSTORE_PATH
javax.net.ssl.trustStoreType=jks
EOF
}

#runTests package test
#runs a speciifc junit test
runTests() {
  echo "---------------------------------------------"
  echo "Run Tests $1:$2"
  echo "---------------------------------------------"
  #$MVN -pl $1 -q -Dconfig.file=../config/application.properties -Dtest=$2 test
  $MVN -pl $1 -Dconfig.file=../config/application.properties -Dtest=$2 test
}

#cleanup name
#Kill apps and docker instances. Cleans up folders
cleanup() {
  echo "---------------------------------------------"
  echo "Cleanup"
  echo "---------------------------------------------"
  docker ps -flabel=casquatch_test -q | xargs -I % docker kill %
  docker container ls -al -flabel=casquatch_test -q | xargs -I % docker rm %
  docker container ls -al -flabel=casquatch_example -q | xargs -I % docker rm %
  $MVN clean
  rm -rf cassandramodels
  rm -rf config/application.properties
}

#showStatus returnCode
#Shows return code as a colored status
showStatus() {
  RED="\033[1;31m"
  GREEN="\033[1;32m"
  NOCOLOR="\033[0m"
  if [ $1 -eq 0 ]; then
    echo "${GREEN}[Success]${NOCOLOR}"
  else
    echo "${RED}[Fail]${NOCOLOR}"
    exit;
  fi
}

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

#buildGenerator
#compiles the generator
buildGenerator() {
  echo "---------------------------------------------"
  echo "Building Generator"
  echo "---------------------------------------------"
  $MVN -pl casquatch-generator -q clean package
}


#generateModels
#Generates the models
generateModels() {
  echo "---------------------------------------------"
  echo "Generating Models"
  echo "---------------------------------------------"
  $JAVA -Dconfig.file=config/application.properties -jar casquatch-generator/target/casquatch-generator-$VERSION.jar
}

#testModels
#test the generated models
testModels() {
  echo "---------------------------------------------"
  echo "Testing Models"
  echo "---------------------------------------------"
  cd cassandramodels
  $MVN clean test
  cd ..
}

#installCasquatch
installCasquatch() {
  echo "---------------------------------------------"
  echo "Installing Casquatch"
  echo "---------------------------------------------"
  $MVN -Dtest.skip=true clean install
}

#basicTestSuite name
#Runs the basic junit test suite
basicTestSuite() {
  echo "[$1][Install Casquatch]" \\c
  installCasquatch >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Run Docker Tests]" \\c
  runTests casquatch-driver-tests *ExternalTests >> $OUTPUT 2>&1
  showStatus $?
}


exampleTestSuite() {
    echo "[$1][Build Generator]" \\c
    buildGenerator >> $OUTPUT 2>&1
    showStatus $?

    echo "[$1][Test Example - LoadTest]" \\c
    casquatch-examples/test.sh loadtest >> $OUTPUT 2>&1
    showStatus $?

    echo "[$1][Test Example - Spring Config Server]" \\c
    casquatch-examples/test.sh springconfigserver >> $OUTPUT 2>&1
    showStatus $?

    echo "[$1][Test Example - Spring Rest]" \\c
    casquatch-examples/test.sh springrest >> $OUTPUT 2>&1
    showStatus $?
}

#generatorTestSuite
#Test suite for code generator
generatorTestSuite() {
  echo "[$1][Install Tables]" \\c
  installTables $container >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Build Generator]" \\c
  buildGenerator >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Generate Models]" \\c
  generateModels >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Test Models]" \\c
  testModels >> $OUTPUT 2>&1
  showStatus $?

}

#solrTestSuite
#test suite for solr
solrTestSuite() {
  #TODO
  echo "[$1][Run JUnit Solr Tests]" \\c
  runTests cassandradriver CassandraDriverDockerSolrTests >> $OUTPUT 2>&1
  showStatus $?
}

#embeddedTests
#basic embedded tests
embeddedTests() {
  echo "[Embedded][Install Casquatch]" \\c
  installCasquatch >> $OUTPUT 2>&1
  showStatus $?

  echo "[Embedded][Run JUnitTests]" \\c
  $MVN -pl casquatch-driver-tests -q -Dtest=*EmbeddedTests test >> $OUTPUT 2>&1
  showStatus $?
}

#cassandraTests version
#run all tests for open source cassandra on the specified version
cassandraTests() {
  version=$1
  container="Cassandra-"$version

  echo "[$container][Cleanup]" \\c
  cleanup $container >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Start Docker]" \\c
  startCluster cassandra:$version $container >>  $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Install Keyspace]" \\c
  installKeyspace $container >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Configure Driver]" \\c
  configureDriver $container $KEYSPACE >> $OUTPUT 2>&1
  showStatus $?

  basicTestSuite  $container
  generatorTestSuite $container
  exampleTestSuite $container

  echo "[$container][Cleanup]" \\c
  cleanup $container >> $OUTPUT 2>&1
  showStatus $?
}

#dseTests version
#run all tests for datastax cassandra on the specified version
dseTests() {
  version=$1
  container="DSE-"$version

  echo "[$container][Cleanup]" \\c
  cleanup $container >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Start Docker]" \\c
  startCluster datastax/dse-server:$version $container >>  $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Install Keyspace]" \\c
  installKeyspace $container >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Configure Driver]" \\c
  configureDriver $container $KEYSPACE >> $OUTPUT 2>&1
  showStatus $?

  basicTestSuite $container
  generatorTestSuite $container
  exampleTestSuite $container

#  echo "[$1][Run EE External Tests]" \\c
#  runTests casquatch-driver-ee-tests *ExternalTests >> $OUTPUT 2>&1
#  showStatus $?

  echo "[$container][Cleanup]" \\c
  cleanup $container >> $OUTPUT 2>&1
  showStatus $?
}

#Create network for instances to use
docker network create cassandra_test_default > /dev/null 2>&1

#Run all tests
embeddedTests
cassandraTests 3.0
cassandraTests 3.11
dseTests 5.1.13
dseTests 6.0.6
dseTests 6.7.2

#Remove the network
docker network rm cassandra_test_default > /dev/null 2>&1
