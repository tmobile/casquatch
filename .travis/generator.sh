#!/bin/bash

source .travis/env.sh

log "Generating Models"
$JAVA -Dconfig.file=config/application.properties -jar casquatch-generator/target/casquatch-generator-$VERSION.jar
showStatus $?

log "Testing Models"
cd cassandramodels
$MVN test
showStatus $?
cd ..