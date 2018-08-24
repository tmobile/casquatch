<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

  <groupId>com.tmobile.opensource.casquatch.${schema}</groupId>
	<artifactId>CassandraGenerator-Models-${schema}</artifactId>
	<version>1.3-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>CassandraGenerator Models - ${schema}</name>
	<description>CassandraGenerator</description>

    <dependencies>
        <dependency>
            <groupId>com.tmobile.opensource.casquatch</groupId>
            <artifactId>CassandraDriver</artifactId>
            <version>1.3-SNAPSHOT</version>
        </dependency>
    </dependencies>

    <properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
    </properties>
</project>
