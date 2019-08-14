#!/bin/bash

source .travis/env.sh

log "Running ExternalTests"
runTests casquatch-driver-tests *ExternalTests
showStatus $?