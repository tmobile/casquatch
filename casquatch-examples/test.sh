#!/bin/bash

if [ -d "casquatch-examples" ]; then
	cd casquatch-examples
fi;

source ./.setup.sh

echo "---------------------------------------------"
echo "Test App"
echo "---------------------------------------------"
$TEST_COMMAND;