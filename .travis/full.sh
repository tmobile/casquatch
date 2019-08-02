#!/bin/bash

source .travis/env.sh

.travis/cleanup.sh $IMAGE
.travis/start.sh $IMAGE
.travis/generator.sh $IMAGE
.travis/run_external_tests.sh $IMAGE
.travis/run_examples.sh $IMAGE
.travis/cleanup.sh $IMAGE