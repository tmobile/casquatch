#!/bin/bash

docker build -t casquatch-cluster-tests .
docker-compose -f dse-docker.yml -p casquatch-cluster-tests up -d --scale search=2 --scale analytics=0 --scale searchanalytics=0 --remove-orphans --force-recreate
docker run --rm --network casquatch-cluster-tests_default -it casquatch-cluster-tests
docker-compose -f dse-docker.yml -p casquatch-cluster-tests down -v
