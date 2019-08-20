---
title: "Spring Rest - Tutorial"
---

## Overview
This is a simple project that utilizes Spring and Casquatch to provide a Rest API for a given schema

## Working Example: [springrest](https://github.com/tmobile/casquatch/tree/master/casquatch-examples/springrest)

## Tutorial
In this tutorial we are going to step through the creation of a simple project from start to finish.

### Prerequisites
The following prerequisites are required:

1. JDK 8
2. Maven
3. Docker (or a running Cassandra database)

### Spring Initializer
Spring offers the Spring Initializer to quick start a project. We will use this to build out the basic template.

1. Go to https://start.spring.io/
2. Fill out form as follows
   * Project: Maven Project
   * Language: Java
   * Spring Boot: 2.1.7
   * Project Metadata
      * Group: com.tmobile.opensource.casquatch.examples
      * Artifact: springrest
    * Dependencies
      * Spring Web Starter
3. Generate the project
4. Extract and cleanup
{{< highlight bash >}}
unzip springrest.zip
rm -rf .mvn
rm -rf src/main/resources/static
rm -rf src/main/resources/templates
rm mvnw*
rm HELP.md
{{< /highlight >}}

### Setup Database in Docker
If you don't already have a running Cassandra instance to connect to, you can spin one up quickly using Docker.

1. Start Docker
{{< highlight bash >}}
docker run --rm  -p 9042:9042 --name springrest -d cassandra:latest
{{< /highlight >}}
2. Import Schema
{{< highlight bash >}}
docker exec -i springrest cqlsh <<EOF
CREATE KEYSPACE springrest WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE springrest.table_name (
   key_one int,
   key_two int,
   col_one text,
   col_two text,
   PRIMARY KEY (key_one, key_two)
);
EOF
{{< /highlight >}}

### Casquatch Configuration
Now the project is ready for Casquatch Integration by adding dependencies, entity, and required annotations.

1. Add Properties to pom.xml
{{< highlight xml >}}
<casquatch.version>2.0-RELEASE</casquatch.version>
{{< /highlight >}}
2. Add Dependencies to pom.xml
{{< highlight xml >}}
<dependency>
    <groupId>com.tmobile.opensource.casquatch</groupId>
    <artifactId>casquatch-driver-spring</artifactId>
    <version>${casquatch.version}</version>
</dependency>
<dependency>
    <groupId>com.tmobile.opensource.casquatch</groupId>
    <artifactId>casquatch-driver-tests</artifactId>
    <version>${casquatch.version}</version>
    <scope>test</scope>
</dependency>
{{< /highlight >}}
3. Configure Compiler Plugin
{{< highlight xml >}}
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>${java.version}</source>
        <target>${java.version}</target>
        <annotationProcessorPaths>
            <path>
                <groupId>com.tmobile.opensource.casquatch</groupId>
                <artifactId>casquatch-driver-processor</artifactId>
                <version>${casquatch.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
{{< /highlight >}}
4. Add Configuration to src/main/resources/application.properties
{{< highlight ini >}}
casquatch.basic.contact-points.0="127.0.0.1:9042"
casquatch.basic.session-keyspace = springrest
casquatch.basic.load-balancing-policy.local-datacenter=datacenter1
{{< /highlight >}}
5. Create Entity as src/main/java/com/tmobile/opensource/casquatch/examples/springrest/TableName.java.
{{< highlight java >}}
@CasquatchEntity
@Getter @Setter @NoArgsConstructor
public class TableName extends AbstractCasquatchEntity {
   @PartitionKey
   private Integer keyOne;

   @ClusteringColumn(1)
   private Integer keyTwo;

   private String colOne;
   private String colTwo;
}
{{< /highlight >}}
Alternatively, entities can be [generated] ({{< ref "codegenerator.md" >}})
6. Add Annotation to SpringRestApplication.java
{{< highlight java >}}
@CasquatchSpring(generateRestDao = true)
{{< /highlight >}}
7. Run Application
{{< highlight bash >}}
mvn spring-boot:run
{{< /highlight >}}

### Test it out
Now that the application is running, it is ready to start serving queries.

1. Insert Data
{{< highlight bash >}}
curl -X POST "http://localhost:8080/TableName/save" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"payload\": { \"keyOne\": 1, \"keyTwo\": 1,\"colOne\":\"test\",\"colTwo\":\"test2\" }}"
{{< /highlight >}}
1. Get Data
{{< highlight bash >}}
curl -X POST "http://localhost:8080/TableName/get" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"payload\": { \"keyOne\": 1, \"keyTwo\": 1 }}"
{{< /highlight >}}

### Optional Swagger UI
In order to easily interact and see the APIs, you can optionally add Swagger UI.

1. Add property in pom.xml
{{< highlight xml >}}
<swagger.version>2.9.2</swagger.version>
{{< /highlight >}}
2. Add Dependencies to pom.xml
{{< highlight xml >}}
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>${swagger.version}</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>${swagger.version}</version>
</dependency>
{{< /highlight >}}
3. Update SpringRestApplication.java
{{< highlight java >}}
@SpringBootApplication
@CasquatchSpring(generateRestDao = true)
@EnableSwagger2
public class SpringRestApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringRestApplication.class, args);
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.tmobile.opensource.casquatch.examples.springrest"))
				.paths(PathSelectors.any())
				.build();
	}

}
{{< /highlight >}}
4. Go to http://localhost:8080/swagger-ui.html#/table-name-_-rest-dao



