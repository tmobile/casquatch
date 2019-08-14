#!/bin/bash

source .travis/env.sh

runExample() {
    EXAMPLE=$1
    log "Running Example - $EXAMPLE"

    cd $BASEDIR/casquatch-examples/$EXAMPLE

    log "Installing CQL"
    cat schema.cql | docker exec -i $BASENAME cqlsh
    showStatus $?

    log "Generate Entity"
    $JAVA -Dcasquatch.basic.contact-points.0=$SEED_IP:9042 -Dconfig.file=casquatch-examples/$EXAMPLE/src/main/resources/application.conf -Dcasquatch.generator.outputFolder=casquatch-examples/$EXAMPLE -Dcasquatch.basic.load-balancing-policy.local-datacenter=$DATACENTER -jar $GENERATOR
    showStatus $?

    log "Testing"
    $WORKER /bin/bash -c "cd casquatch-examples/$EXAMPLE;mvn -Dcasquatch.basic.contact-points.0=$SEED_IP:9042 -Dcasquatch.basic.load-balancing-policy.local-datacenter=$DATACENTER clean test"
    showStatus $?
}

runExample loadtest
runExample springconfigserver
runExample springrest