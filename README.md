# Project Casquatch

1. [Overview](#overview)
2. [Features](#features)
3. [Implementation](#implementation)
   1. [Install](#install)
   2. [Add to pom.xml](#add-to-pomxml)
   3. [Add to Code](#add-to-code)
4. [Configuration](#configuration)
5. [Feature Details](#feature-details)
   1. [Code Generator](#code-generator)
   2. [Builder Configuration](#builder-configuration)
   3. [Driver Config](#driver-config)
   4. [Driver Cache](#driver-cache)
   5. [Solr](#solr)
   6. [Spring Config](#spring-config)
6. [FAQ](#FAQ)
7. [Release Notes](#release-notes)


## Overview
This project is designed to provide a java abstraction layer for the Cassandra database such that the developers will interact with generated POJOs through simple get, save, delete, procedures without writing a single line of CQL or importing any Datastax packages.

## Features
* Built on top of Datastax Driver for full vendor support
* Pretuned and Configured for High Availability and Geo-Redundancy
  * Load Balancing / Geo-Redundancy
  * Connection Pooling
  * Reconnection Policy
  * Retry Policy
  * Data Caching
  * Dynamic Data Center redirection
* Generates Annotated POJOs based on Cassandra Tables
* Uses prepared statements for performance gains and type safe queries
* Significantly reduces code development effort and reduces code duplication
* Customizations available through standard application.properties
* Supports Spring Autowire

## Implementation
### Install
* Clone
  ```
  git clone -b "X.Y-RELEASE" http://github.com/tmobile/casquatch
  ```
* Configure Project
  * Update config/application.properties as defined in [Configuration](#configuration) section
* Scripted Install (Linux / OS X)
  ```
  cd casquatch
  ./install.sh <KEYSPACE>
  ```
* Manual Install (Windows)
  * Install Driver
    ```
    cd cassandradriver
    mvn install
    ```
  * Run Code Generator
    ```
    java -jar cassandragenerator/target/CassandraGeneratorX-Y.jar --properties=config/application.properties --package --output=cassandramodels
    cd cassandramodels
    mvn install
    ```
  * Generator Javadocs
    ```
    mvn javadoc:aggregate
    ```

### Add to pom.xml
* Add version property
  ```
  <casquatch.version>X.Y-RELEASE</casquatch.version>
  ```
* Add dependency for Driver
  ```
  <dependency>
    <groupId>com.tmobile.opensource.casquatch</groupId>
    <artifactId>CassandraDriver</artifactId>
    <version>${casquatch.version}</version>
  </dependency>
  ```
* Add dependency for Models (If installed above)
  ```
  <dependency>
    <groupId>com.tmobile.opensource.casquatch.<KEYSPACE></groupId>
    <artifactId>CassandraGenerator-Models-<KEYSPACE></artifactId>
    <version>${casquatch.version}</version>
  </dependency>
  ```
### Update Application.java
* Find @SpringBootApplication and add this line:
  ```
  @Import(CassandraDriverSpringConfiguration.class)
  ```

### Add to Code
* Imports
  ```
  import com.tmobile.opensource.casquatch.CassandraDriver;
  import com.tmobile.opensource.casquatch.models.<KEYSPACE>.*;
  import org.springframework.beans.factory.annotation.Autowired;
  ```
* Add variable
  ```
  @Autowired
  private CassandraDriver db;
  ```

## Configuration
| Property | Required | Default | Description |
| ---|---|---|---|
| cassandraDriver.username | | cassandra | Database User |
| cassandraDriver.password | | cassandra | Database Password |
| cassandraDriver.contactPoints | Yes | | Comma separated list of contact points |
| cassandraDriver.port | | 9042 | Native port |
| cassandraDriver.localDC | Yes | | Name of local data center |
| cassandraDriver.keyspace | Yes | | Database Keyspace |
| cassandraDriver.defaults.consistencyLevel | | LOCAL_QUORUM | Set default consistency level |
| cassandraDriver.defaults.clusterType | | HA | Specify an HA (Multi DC) or Single DC cluster |
| cassandraDriver.defaults.solrDC | | search | Specify datacenter for solr |
| cassandraDriver.defaults.saveNulls | | disabled | Enable or disable saving of null values. (This will generate tombstones) |
| cassandraDriver.useRemoteConnections | | 2 | Specify number of remote connections allowed for LOCAL consistency |
| cassandraDriver.connections.local.min | | 1 | Minimum number of local connections (Min and Max Required) |
| cassandraDriver.connections.local.max | | 3 | Maximum number of local connections (Min and Max Required) |
| cassandraDriver.connections.remote.min | | 1 | Minimum number of remote connections (Min and Max Required) |
| cassandraDriver.connections.remote.max | | 1 | Maximum number of remote connections (Min and Max Required) |
| cassandraDriver.loadBalancing.filter.token | | enabled | Toggle TokenAwareLoadBalancingPolicy |
| cassandraDriver.loadBalancing.filter.dcs | | | Comma separated list of datacenters |
| cassandraDriver.loadBalancing.filter.workloads | | | Comma separated list of workloads (EE Only) |
| cassandraDriver.speculativeExecution.delay | | 500 | Number of ms to wait before implementing Speculative Execution |
| cassandraDriver.speculativeExecution.executions | | 2 | Max number of Speculative Executions |
| cassandraDriver.timeout.read | | 500 | Set read timeout in ms |
| cassandraDriver.timeout.connection | | 12000 | Set connection timeout in ms |
| cassandraDriver.reconnection.delay | | 500 | Set delay between reconnection. On failure, this will be exponential increased up to maxDelay |
| cassandraDriver.reconnection.maxDelay | | 300000 | Set max delay between reconnection attempts |
| cassandraDriver.features.driverConfig | | enabled | Enable or Disable Driver Config table |
| cassandraDriver.features.solr | | enabled | Enable or Disable Solr searches |
| cassandraDriver.ssl.node | | disabled | Enable or disable client to node ssl |
| cassandraDriver.ssl.truststore.path | | | Path to JKS containing trusted cluster certificate(s) |
| cassandraDriver.ssl.truststore.password | | | Password of truststore |
| security.user.name | | | SPRING CONFIG SERVER ONLY : Username |
| security.user.password | | | SPRING CONFIG SERVER ONLY : Password |
| cassandraGenerator.db.username | | cassandra | Database User |
| cassandraGenerator.db.password | | cassandra | Database Password |
| cassandraGenerator.db.contactPoints | Yes | | Comma separated list of contact points |
| cassandraGenerator.db.port | | 9042 | Native port |
| cassandraGenerator.db.datacenter | Yes | | Name of local data center |
| cassandraGenerator.db.keyspace | Yes | | Database Keyspace |
| cassandraGenerator.output.folder | | ./ | Folder for output files |
| cassandraGenerator.output.overwrite | | false | Allow overwriting of files |
| cassandraGenerator.output.mode | | console | file or console mode |
| cassandraGenerator.package | | false | Structure output for a maven package |
| cassandraGenerator.tables | | | CSV List of tables |
| cassandraGenerator.cachableTables | | | CSV List of cachable tables |
| cassandraGenerator.types | | | CSV List of types |
| cassandraGenerator.packageName | | com.tmobile.opensource.casquatch.models.$KEYSPACE$ | Package name for generated code. Replaces $KEYSPACE$ with keyspace name |

## Feature details

### Code Generator
The code generator reverse engineers the schema to create POJOs with Datastax annotations. This is used during the install script but can also be run manually by downloading the jar.
* Configure Project as defined in [Configuration](#configuration) section
* Generate using properties file: ```java -jar CassandraGenerator.jar --properties=application.properties```
* Generate all tables in a keyspace providing minimum information: ```java -jar CassandraGenerator.jar --output=tmp --keyspace=myKeyspace --datacenter=dkr```
* Generate a package for all tables in a keyspace providing additional information: ```java -jar CassandraGenerator.jar --output=tmp --contactPoints=localhost --port=9042 --keyspace=myKeyspace --datacenter=dkr --user=cassandra --password=cassandra --package --packageName=com.test.myapp --overwrite```
* Optionally install locally via maven (if --package was used): ```mvn install```

### Builder Configuration
While Spring is the simplest method of configuration, the driver also supports the builder pattern for configuration. (See Javadoc CassandraDriver.Builder for specifics). This allows a driver to be built explicitly similar to the following. All settings will be defaulted as defined above with options to configure as necessary.

* Basic Driver
  ```
  db = new CassandraDriver.Builder()
                  .withContactPoints("host1.domain,host2.domain")
                  .withLocalDC("DC1")
                  .withKeyspace("system")
                  .build();
  ```
* Basic Driver with Authentication
  ```
  db = new CassandraDriver.Builder()
                 .withContactPoints("host1.domain,host2.domain")
                 .withLocalDC("DC1")
                 .withKeyspace("system")
                 .withUsername("admin")
                 .withPassword("admin")
                 .build();
  ```
* Advanced Example
  ```
  db = new CassandraDriver.Builder()
                 .withContactPoints("host1.domain,host2.domain")
                 .withLocalDC("DC1")
                 .withKeyspace("system")
                 .withUsername("admin")
                 .withPassword("admin")
                 .withReadTimeout(1000)
                 .withSolr()
                 .withSolrDC("solr")
                 .withoutDriverConfig()                          
                 .build();
  ```
### Driver Config
The driver_config table is an optional table (enabled by default) that can be loaded from cassandradriver/driver_config.cql. This table allows one to specify data center, read consistency, and write consistency on a per table basis. The driver will consult this table on every to find the appropriate settings. If the row is missing it will look for a table named default. Responses are cached for 15 minutes to reduce overhead.

### Driver Cache
The DriverCache interface is a very simply lazy caching mechanism to allow for objects to be queried with a predefined timeout. To implement this, you will first need to implement AbstractCachable to define a getCacheKey and a setCacheKey procedure. Typically this can just be a concatenation of the primary keys. Then you can use code similar to:
```
@Autowired
CassandraDriver db

DatabaseCache<MyObj> myObjCache = new DatabaseCache<>(MyObj.class,db);

MyObj obj = myObjCache.get('key1.key2');
```
The cache is updated in the following conditions:
a) Data is requested via get and it does not exist in cache. It is then queried from the database.
b) Data is requested via get and the timeout has expired (default 15 minutes). This is treated the same as a cache miss and data is thus queried from the Database
c) A set is called, then the same data is inserted to the cache

### Solr
Datastax solr queries are available with multiple interfaces. This requires that solr be set up and configured on the Cassandra database server.
DSE 5.1 - Query by JSON with the getAllBySolr(class,solrQuery, For details on the format of the solrQuery, please consult https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax for details.
DSE 6.0 - Adds Query by Object and Query by CQL with getAllBySolr(class,object) and getAllBySolr(class,cql) respectively. See https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/siQuerySyntax.html for details

### Spring Config
Casquatch contains an implementation of the Spring Config Server (https://cloud.spring.io/spring-cloud-config/single/spring-cloud-config.html) with a Cassandra backend using the included driver. Schema is available at springconfigserver/schema.cql.

In addition to the [cassandraDriver properties](#configuration), the following properties must exist in config/application.properties:
```
security.user.name=
security.user.password=
server.port=8888
```

At a high level, this allows the configuration that is typically in application.properties to be moved to a central database. With this, the client pom.xml has a few added dependencies (https://cloud.spring.io/spring-cloud-config/single/spring-cloud-config.html#_client_side_usage), as well as a bootstrap.properties (see below) that contain the spring config server details. All other properties are pulled from the database.

The bootstrap would look something like:
```
spring.application.name=app_name
spring.profiles.active=profile_name
spring.cloud.config.uri=http://spring_config_server:8888
spring.cloud.config.username=spring_config_username
spring.cloud.config.password=spring_config_password
```

## FAQ
### What Cassandra driver does Casquatch use?
Casquatch uses the open source Datastax Driver (https://github.com/datastax/java-driver) which is compatible with both Apache Cassandra and Datastax Enterprise. If you wish to use the closed source driver you can do so by following the instructions at [Datastax Driver FAQ](https://docs.datastax.com/en/developer/java-driver-dse/1.6/faq/#how-do-i-use-this-driver-as-a-drop-in-replacement-for-cassandra-driver-core-when-it-is-a-dependency-of-another-project). Alternatively, you can use the CassandraDriver-EE ArtifactId that has already been configured with the correct versions and is simply a wrapper implementing the above.
### Why do i get ClassNotFoundException with Spring Boot and SSL enabled?
Spring Boot contains a dependency for com.datastax.cassandra:cassandra-driver-core which overrides the version in CassandraDriver. When using a custom truststore, this exception may be seen at runtime:
```
java.lang.ClassNotFoundException: com.datastax.driver.core.RemoteEndpointAwareJdkSSLOption
```
This can be resolved by adding the following to your pom.xml
```
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.datastax.cassandra</groupId>
      <artifactId>cassandra-driver-core</artifactId>
      <version>3.6.0</version>
    </dependency>
    <dependency>
      <groupId>com.datastax.cassandra</groupId>
      <artifactId>cassandra-driver-mapping</artifactId>
      <version>3.6.0</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Release Notes
### 1.4-SNAPSHOT - TBD
* Full rewrite of generator to switch from web to command line
* Added functionality for filtering on either workload or DC
* Minor - Added getVersion() which shows Casquatch and driver versions
### 1.3-RELEASE - 09/17/2018
* Added SSL Support
* Added CassandraDriver-EE as a simple wrapper to inject the licensed driver.
* Additional Documentation and Tests
* Updated to latest Cassandra Driver (3.6.0)
* Added support for Solr Query By Object and by CQL. Requires DSE Search >= 6.0
* Rewrote test suite to fully regression test multiple versions
* Bugfix: Spring Config Server changes to run independently
### 1.2-RELEASE - 06/22/2018
* Initial Open Source Release
* Added Spring Config Server
* Extended all tuning properties to be modified using a builder procedure or spring configuration
* Moved all spring references into CassandraDriverSpringConfiguration
* Moved from log4j to logback to match spring
* Renamed package and group ids for open source
* Added licensing and documentation for open source
### 1.1.1-RELEASE - 05/02/2018
* Model Generator Bug Fixes
  * Additional additional types in generator
  * Backwards compatibility to 5.0
  * Added Full UDT Support
  * Switched from preferring primitives to preferring classes. Multiple bugs with UDT and tombstone creation
### 1.1-RELEASE - 04/05/2018
* Added ASync versions of most calls
* Added Solr Supports
* Added getAllById - Allows getting all of a partition with multiple clustering keys
* Added CassandraAdminDriver - Allows a direct driver connection for advanced use cases
* Merged generator and driver into one project
* Added this documentation
* Added automated installer script
* Improved exception handling
* General bugfixes and code cleanup
### 1.0-RELEASE - 03/05/2018
* Initial Release
