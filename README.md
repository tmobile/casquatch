# Project Casquatch

## Overview
This project is designed to provide a java abstraction layer for the Cassandra database such that the developers will interact with generated POJOs through simple get, save, delete, procedures without writing a single line of CQL or importing any Datastax packages.

## Documentation
The full manual is available at http://tmobile.github.io/casquatch

## Quick Start
* Download Generator from releases
* Run Code Generator - [Code Generator](https://tmobile.github.io/casquatch/features/codegenerator/)
```
java -Dcasquatch.generator.outputFolder=src/main/java/com/demo/mykeyspace -Dcasquatch.generator.packageName=com.demo.mykeyspace -Dcasquatch.generator.keyspace=myKeyspace -Dcasquatch.generator.datacenter=datacenter1 -jar CassandraGenerator.jar
```
* Configure Maven - [Maven Configuration](https://tmobile.github.io/casquatch/features/mavenconfiguration/)
* Optionally configure Spring - [Configure Spring](https://tmobile.github.io/casquatch/features/configurespring/)
* Enjoy! - [API](https://tmobile.github.io/casquatch/features/api/)
* Check out [Examples](https://tmobile.github.io/casquatch/examples/) for sample projects

## Release Notes
Release notes are now maintained in [Manual](https://tmobile.github.io/casquatch/releasenotes/)
