---
title: "Code Generator"
---
## Concept
The code generator reverse engineers the schema to create POJOs Entities by evaluating the session metadata.

## Configuration

* See [Generator Configuration]({{< ref "casquatchgenerator.md" >}}) for configuration information

### Examples
* Generate using properties file:
{{< highlight bash >}}
java -Dconfig.file=/path/to/config.properties -jar CassandraGenerator.jar 
{{< /highlight >}}
* Generate all tables in a keyspace providing minimum information:
{{< highlight bash >}}
java -Dcasquatch.generator.outputFolder=tmp -Dcasquatch.generator.keyspace=myKeyspace -Dcasquatch.generator.datacenter=datacenter1 -jar CassandraGenerator.jar
{{< /highlight >}}
* Generate a package for all tables in a keyspace providing additional information:
{{< highlight bash >}}
java -Dcasquatch.generator.outputFolder=tmp -Dcasquatch.generator.keyspace=myKeyspace -Dcasquatch.generator.datacenter=datacenter1 -Dcasquatch.generator.username=cassandra -Dcasquatch.generator.password=cassandra -Dcasquatch.generator.createPackage=true -Dcasquatch.generator.packageName=com.demo.mykeyspace -jar CassandraGenerator.jar
{{< /highlight >}}
