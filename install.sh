#!/bin/bash
#
# Casquatch Installer - See Readme.md for more information
#

CASQUATCH_KEYSPACE=$(echo $1 | tr '[:upper:]' '[:lower:]')
CASQUATCH_VERSION=1.3-SNAPSHOT

if [ -z "$CASQUATCH_KEYSPACE" ]; then
  echo "ERROR: Please provide a keyspace. See README.md for more information.";
  exit 1;
fi;

if [ ! -f config/application.properties ]; then
  echo "ERROR: You must configure config/application.properties. See README.md for more information.";
  exit 1;
fi;

if ps -ef | grep -v grep | grep -s 8080; then
  echo "ERROR: Port 8080 is already in use"
  exit 1;
fi;

echo "This will install the current driver as well as generator models for the $CASQUATCH_KEYSPACE keyspace. If ${CASQUATCH_KEYSPACE}_models exists it will be deleted"
read -p "Press enter to continue"
echo "---------------------------------------------"
echo "Cleaning up project"
echo "---------------------------------------------"
mvn -q clean >> /dev/null
rm -rf cassandramodels

echo "---------------------------------------------"
echo "Installing driver"
echo "---------------------------------------------"
mvn -pl cassandradriver -q clean install

echo "---------------------------------------------"
echo "Running Generator (Log at nohup.out)"
echo "---------------------------------------------"
nohup mvn -pl cassandragenerator spring-boot:run > nohup.out 2>&1 &
echo "Waiting for startup"
until grep -q "Started Application" nohup.out; do sleep 1; done;

echo "---------------------------------------------"
echo "Generating Models"
echo "---------------------------------------------"
mkdir cassandramodels
cd cassandramodels
curl http://localhost:8080/generator/${CASQUATCH_KEYSPACE}/download/bash | bash > /dev/null 2>&1
pkill -f -9 cassandragenerator > /dev/null 2>&1
cd ..

if [ ! -f cassandramodels/pom.xml ]; then
  echo "ERROR: Failed to generate models. Please check nohup.out for errors";
  exit 1;
fi;

echo "---------------------------------------------"
echo "Installing Models"
echo "---------------------------------------------"
mvn -f pom_install.xml -pl cassandramodels -q clean install
mvn -f pom_install.xml -pl cassandramodels -q clean

echo "---------------------------------------------"
echo "Generating Javadocs"
echo "---------------------------------------------"
mvn -f pom_install.xml javadoc:aggregate > /dev/null

echo "---------------------------------------------"
echo " CASQUATCH INSTALL COMPLETE FOR ${CASQUATCH_KEYSPACE}"
echo "---------------------------------------------"
echo "Javadocs at target/site/apidocs/index.html"
echo "---------------------------------------------"
cat <<EOF
Please add the following to your pom.xml
<dependency>
    <groupId>com.tmobile.opensource.casquatch</groupId>
    <artifactId>CassandraDriver</artifactId>
    <version>$CASQUATCH_VERSION</version>
</dependency>
<dependency>
    <groupId>com.tmobile.opensource.casquatch.${CASQUATCH_KEYSPACE}</groupId>
    <artifactId>CassandraGenerator-Models-${CASQUATCH_KEYSPACE}</artifactId>
    <version>$CASQUATCH_VERSION</version>
</dependency>
EOF
