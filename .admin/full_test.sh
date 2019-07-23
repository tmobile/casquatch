#!/bin/sh
#
# Runs through tests of the full flow and project
#
DATACENTER=dc1
DATACENTER2=dc2
KEYSPACE=fulltest
OUTPUT=.admin/test.out
PORT="90"$(( ( RANDOM % 50 )  + 42 ))
VERSION=2.0-SNAPSHOT

#startDockerCassandra name version
#Start up open source cassandra for the given version
startDockerCassandra() {
  echo "---------------------------------------------"
  echo "Starting Cassandra Docker with DC=$DATACENTER"
  echo "---------------------------------------------"
  docker run --rm  -p $PORT:9042 -d -e CASSANDRA_DC=$DATACENTER -e CASSANDRA_ENDPOINT_SNITCH=GossipingPropertyFileSnitch --label casquatch_test=$1 --name $1 -d cassandra:$2
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it $1 nodetool status 2>/dev/null | grep rack1 | grep -q 'UN'" 120
  else
    $?
  fi;
}


#startDockerDSECluster name version
#Start up a datastax cassandra cluster with mulitple nodes for the given version
startDockerCassandraCluster() {
  echo "---------------------------------------------"
  echo "Starting Docker Cassandra Cluster"
  echo "---------------------------------------------"
  NAME=$1
  VERSION=$2
  docker network create ${NAME}_default --label casquatch_test=${NAME}
  docker run --network ${NAME}_default --rm -e CASSANDRA_DC=$DATACENTER -e CASSANDRA_ENDPOINT_SNITCH=GossipingPropertyFileSnitch -d --name ${NAME}_seed_1 --label casquatch_test=${NAME} -d cassandra:$VERSION
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it ${NAME}_seed_1 dsetool status 2>/dev/null | grep rack1 | grep -q UN" 120
  else
    $?
  fi;
  docker run --network ${NAME}_default --rm -e CASSANDRA_DC=$DATACENTER2 -e CASSANDRA_ENDPOINT_SNITCH=GossipingPropertyFileSnitch -e CASSANDRA_SEEDS=${NAME}_seed_1 -d --name ${NAME}_search_1 --label casquatch_test=${NAME} -d cassandra:$VERSION
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it ${NAME}_seed_1 nodetool status 2>/dev/null | grep rack1 | grep -c UN | grep -q '2'" 300
  else
    $?
  fi;
  docker run --network ${NAME}_default --rm -e CASSANDRA_DC=$DATACENTER2 -e CASSANDRA_ENDPOINT_SNITCH=GossipingPropertyFileSnitch -e CASSANDRA_SEEDS=${NAME}_seed_1 -d --name ${NAME}_search_2 --label casquatch_test=${NAME} -d cassandra:$VERSION
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it ${NAME}_seed_1 nodetool status 2>/dev/null | grep rack1 | grep -c UN | grep -q '3'" 300
  else
    $?
  fi;
}

#startDockerDSE name version
#Start up datastax cassandra with solr enabled for the given version
startDockerDSE() {
  echo "---------------------------------------------"
  echo "Starting DSE Docker"
  echo "---------------------------------------------"
  docker run --rm -e DS_LICENSE=accept -p $PORT:9042 -p 9142:9142 -d --name $1 -h dse.local --label casquatch_test=$1 -d datastax/dse-server:$2 -s
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it $1 dsetool status 2>/dev/null | grep rack1 | grep -q 'UN'" 120
  else
    $?
  fi;
}

#startDockerDSECluster name version
#Start up a datastax cassandra cluster with mulitple nodes for the given version
startDockerDSECluster() {
  echo "---------------------------------------------"
  echo "Starting DSE Docker Cluster"
  echo "---------------------------------------------"
  NAME=$1
  VERSION=$2
  docker network create ${NAME}_default --label casquatch_test=${NAME}
  docker run --network ${NAME}_default --rm -e DS_LICENSE=accept -e DC=$DATACENTER -d --name ${NAME}_seed_1 --label casquatch_test=${NAME} -d datastax/dse-server:$VERSION
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it ${NAME}_seed_1 dsetool status 2>/dev/null | grep rack1 | grep -q UN" 120
  else
    $?
  fi;
  docker run --network ${NAME}_default --rm -e DS_LICENSE=accept -e DC=$DATACENTER2 -e SEEDS=${NAME}_seed_1 -d --name ${NAME}_search_1 --label casquatch_test=${NAME} -d datastax/dse-server:$VERSION -s
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it ${NAME}_seed_1 dsetool status 2>/dev/null | grep rack1 | grep -c UN | grep -q '2'" 300
  else
    $?
  fi;
  docker run --network ${NAME}_default --rm -e DS_LICENSE=accept -e DC=$DATACENTER2 -e SEEDS=${NAME}_seed_1 -d --name ${NAME}_search_2 --label casquatch_test=${NAME} -d datastax/dse-server:$VERSION -s
  if [ $? -eq 0 ]; then
    retryLoop "docker exec -it ${NAME}_seed_1 dsetool status 2>/dev/null | grep rack1 | grep -c UN | grep -q '3'" 300
  else
    $?
  fi;
}

#configureDSESSL name
#Configure SSL on DSE
configureDSESSL() {
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
cat << EOF > config/application.properties
casquatch.basic.contact-points.0=localhost:$PORT
casquatch.basic.load-balancing-policy.local-datacenter=$DATACENTER
casquatch.basic.session-keyspace=$1
casquatch.generator.createPackage=true
casquatch.generator.outputFolder=cassandramodels
casquatch.generator.file=true
EOF
  if [ "$1" == "springconfig" ]; then
  cat << EOF >> config/application.properties
security.user.name=test
security.user.password=test
server.port=8888
EOF
  fi;
}

#runTests package test
#runs a speciifc junit test
runTests() {
  echo "---------------------------------------------"
  echo "Run Tests $1:$2"
  echo "---------------------------------------------"
  mvn -pl $1 -q -Dconfig.file=../config/application.properties -Dtest=$2 test
}

#startSpringConfig
#Start up spring config server and wait for it to come up
startSpringConfig() {
  echo "---------------------------------------------"
  echo "Starting Spring Config"
  echo "---------------------------------------------"
  nohup mvn -pl springconfigserver spring-boot:run > nohup.out 2>&1 &
  retryLoop "grep -q 'Started Application' nohup.out" 120
}

#cleanup name
#Kill apps and docker instances. Cleans up folders
cleanup() {
  echo "---------------------------------------------"
  echo "Cleanup"
  echo "---------------------------------------------"
  docker ps -flabel=casquatch_test -q | xargs -I % docker kill %
  docker container ls -al -flabel=casquatch_test -q | xargs -I % docker rm %
  docker network ls -flabel=casquatch_test -q | xargs -I % docker network rm %
  mvn clean
  rm -rf cassandramodels
  rm -rf config/application.properties
}

#testSpringConfig
#Test the running spring config server
testSpringConfig() {
  echo "---------------------------------------------"
  echo "Pulling test config. Should be keyname: valuevalue"
  echo "---------------------------------------------"
  curl -s -u test:test http://localhost:8888/app-test.yml | grep -q "keyname: valuevalue"
  #curl -u test:test http://localhost:8888/app/test/labelname
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
  mvn -pl casquatch-generator -q clean package
}


#generateModels
#Generates the models
generateModels() {
  echo "---------------------------------------------"
  echo "Generating Models"
  echo "---------------------------------------------"
  java -Dconfig.file=config/application.properties -jar casquatch-generator/target/casquatch-generator-$VERSION.jar
}

#testModels
#test the generated models
testModels() {
  echo "---------------------------------------------"
  echo "Testing Models"
  echo "---------------------------------------------"
  cd cassandramodels
  mvn -q clean test
  cd ..
}

#installCasquatch
installCasquatch() {
  echo "---------------------------------------------"
  echo "Installing Casquatch"
  echo "---------------------------------------------"
  mvn -q clean install
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

#generatorTestSuite
#Test suite for code generator
generatorTestSuite() {
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

#springTestSuite
#test suite for spring
springTestSuite() {
  #TODO
  echo "[$1][Install Spring Config Server Keyspace]" \\c
  installSpringKeyspace $1 >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Configure Driver - Spring Config Server]" \\c
  configureDriver springconfig >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Start Spring Config Server]" \\c
  startSpringConfig >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Test Spring Config Server]" \\c
  testSpringConfig >> $OUTPUT 2>&1
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

#sslTestSuite
#test suite for DSE SSL
sslTestSuite() {
  #TODO
  echo "[$1][Config SSL]" \\c
  configureDSESSL $1 $version >> $OUTPUT 2>&1
  showStatus $?

  echo "[$1][Run JUnit SSL Tests]" \\c
  export MAVEN_OPTS="-Djavax.net.ssl.trustStorePassword=cassandra -Djavax.net.ssl.trustStore=$TRUSTSTORE_PATH -Djavax.net.ssl.trustStoreType=jks"
  runTests cassandradriver CassandraDriverDockerSSLTests >> $OUTPUT 2>&1
  showStatus $?
}

#embeddedTests
#basic embedded tests
embeddedTests() {
  echo "[Embedded][Install Casquatch]" \\c
  installCasquatch >> $OUTPUT 2>&1
  showStatus $?

  echo "[Embedded][Run JUnitTests]" \\c
  mvn -pl casquatch-driver-tests -q -Dtest=*EmbeddedTests test >> $OUTPUT 2>&1
  showStatus $?
}

runClusterTests() {
  #TODO
  NAME=$1
  mvn -q -pl cassandradriver -P cluster-tests clean package
  docker cp cassandradriver/target/CassandraDriver-*-test-jar-with-dependencies.jar ${NAME}_seed_1:/cassandra-driver-cluster-tests.jar
  docker exec -it ${NAME}_seed_1 java -jar /cassandra-driver-cluster-tests.jar
}

runClusterEETests() {
  #TODO
  mvn -q -pl cassandradriver-ee -P cluster-tests clean package
  docker cp cassandradriver-ee/target/CassandraDriver-*-test-jar-with-dependencies.jar ${NAME}_search_1:/cassandra-driver-cluster-ee-tests.jar
  docker exec -it ${NAME}_search_1 java -jar /cassandra-driver-cluster-ee-tests.jar
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
  startDockerCassandra $container $version >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Install Keyspace]" \\c
  installKeyspace $container >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Configure Driver]" \\c
  configureDriver $KEYSPACE >> $OUTPUT 2>&1
  showStatus $?

  basicTestSuite  $container
  generatorTestSuite $container
#  springTestSuite $container

  echo "[$container][Cleanup]" \\c
  cleanup $container >> $OUTPUT 2>&1
  showStatus $?

#  TODO
#  echo "[$container][Start Cassandra Cluster]" \\c
#  startDockerCassandraCluster $container $version >> $OUTPUT 2>&1
#  showStatus $?
#
#  echo "[$container][Run Cluster Tests]" \\c
#  runClusterTests $container >> $OUTPUT 2>&1
#  showStatus $?
#
#  echo "[$container][Cleanup]" \\c
#  cleanup $container >> $OUTPUT 2>&1
#  showStatus $?
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
  startDockerDSE $container $version >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Install Keyspace]" \\c
  installKeyspace $container >> $OUTPUT 2>&1
  showStatus $?

  echo "[$container][Configure Driver]" \\c
  configureDriver $KEYSPACE >> $OUTPUT 2>&1
  showStatus $?

  basicTestSuite $container
  generatorTestSuite $container
  #TODO
#  springTestSuite $container
#  solrTestSuite $container
#  sslTestSuite $container

#  echo "[$container][Run JUnit Enterprise Driver Tests]" \\c
#  runTests cassandradriver-ee CassandraDriverEEDockerTests >> $OUTPUT 2>&1
#  showStatus $?

  echo "[$container][Cleanup]" \\c
  cleanup $container >> $OUTPUT 2>&1
  showStatus $?
  #TODO
#  echo "[$container][Start DSE Cluster]" \\c
#  startDockerDSECluster $container $version >> $OUTPUT 2>&1
#  showStatus $?
#
#  echo "[$container][Run EE Cluster Tests]" \\c
#  runClusterEETests $container >> $OUTPUT 2>&1
#  showStatus $?
#
#  echo "[$container][Cleanup]" \\c
#  cleanup $container >> $OUTPUT 2>&1
#  showStatus $?
}

#Run all tests
embeddedTests
cassandraTests 3.0
cassandraTests 3.11
dseTests 5.1.13
dseTests 6.0.6
dseTests 6.7.2
