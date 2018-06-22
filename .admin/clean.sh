#!/bin/sh
#
# Cleaner script
#

mvn clean
cd cassandragenerator
mvn clean
rm nohup.out
cd ..
cd cassandradriver
mvn clean
cd ..
cd springconfigserver
mvn clean
rm nohup.out
cd ..
rm -rf cassandramodels
