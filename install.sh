#!/bin/bash
#
# Casquatch Installer - See Readme.md for more information
#

CASQUATCH_KEYSPACE=$(echo $1 | tr '[:upper:]' '[:lower:]')
CASQUATCH_VERSION=1.4.1-SNAPSHOT

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

echo "This will install the current driver as well as generator models for the $CASQUATCH_KEYSPACE keyspace. If cassandramodels exists it will be deleted"
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
echo "Compile Generator (Log at nohup.out)"
echo "---------------------------------------------"
mvn -q -pl cassandragenerator package

echo "---------------------------------------------"
echo "Generating Models"
echo "---------------------------------------------"
java -jar cassandragenerator/target/CassandraGenerator-$CASQUATCH_VERSION.jar --properties=config/application.properties --package --output=cassandramodels  >> nohup.out 2>&1

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
    <groupId>com.tmobile.opensource.casquatch.models.${CASQUATCH_KEYSPACE}</groupId>
    <artifactId>CassandraGenerator-Models-${CASQUATCH_KEYSPACE}</artifactId>
    <version>$CASQUATCH_VERSION</version>
</dependency>
EOF
