---
title: "Configuration - Generator"
---

Properties can be passed either through a -D parameter or in a properties file along with -Dconfig.file=<path>

| Property | Type | Required | Default | Description |
| ---|---|---|---|---|
| casquatch.generator.username | String | | | Authentication Username |
| casquatch.generator.password | String | | | Authentication Password |
| casquatch.generator.keyspace | String | Yes | | Keyspace to parse |
| casquatch.generator.datacenter | String | | Yes | | LocalDC for Connection |
| casquatch.generator.contactPoints | String Array | No | 127.0.0.1:9042 | Cassandra Contact Point |
| casquatch.generator.tables | String Array | No | | Provide a list of tables. All tables are processed if not supplied |
| casquatch.generator.console | Boolean | No | false | Toggle console output |
| casquatch.generator.file | Boolean | No | false | Toggle file output |
| casquatch.generator.outputFolder | String | No | | Required if file=true. Defines location to write generated files |
| casquatch.generator.overwrite | Boolean | No | false | Toggle overwriting of files in outputFolder |
| casquatch.generator.createPackage | Boolean | No | false | If createPackage=true then pom.xml and src folder structure will be added |
| casquatch.generator.packageName | String | No | com.tmobile.opensource.casquatch.models | Package name for source files |
| casquatch.minify | Boolean | No | false | Create entity with minimal amount of code by excluding convenience methods |
| casquatch.createTests | String | No | true | Create test entities as well |
| config.file | String | No | Specify a path  to a config file to place parameters |