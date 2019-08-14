<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

  <groupId>${package}</groupId>
	<artifactId>CassandraGenerator-Models-${keyspace}</artifactId>
	<version>1.5-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>CassandraGenerator Models - ${keyspace}</name>
	<description>CassandraGenerator</description>

    <dependencies>
        <dependency>
            <groupId>com.tmobile.opensource.casquatch</groupId>
            <artifactId>CassandraDriver</artifactId>
            <version>1.5-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.tmobile.opensource.casquatch</groupId>
            <artifactId>CassandraDriver</artifactId>
            <version>1.5-SNAPSHOT</version>
            <classifier>tests</classifier>
            <type>test-jar</type>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.cassandraunit</groupId>
            <artifactId>cassandra-unit</artifactId>
            <version>3.3.0.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>uk.co.jemos.podam</groupId>
            <artifactId>podam</artifactId>
            <version>7.2.3.RELEASE</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.19.1</version>
                <configuration>
                    <includes>
                        <include>**/*CassandraGenerator*Tests.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
