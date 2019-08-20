<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright 2018 T-Mobile US, Inc.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.tmobile.opensource.casquatch</groupId>
        <artifactId>casquatch-parent</artifactId>
        <version>2.0-RELEASE</version>
    </parent>

    <artifactId>casquatch-generator-models-${keyspace}</artifactId>
    <packaging>jar</packaging>

    <name>Casquatch Generator Models - ${keyspace}</name>
    <description>Casquatch Generator Models - ${keyspace}</description>

    <dependencies>
        <dependency>
            <groupId>com.tmobile.opensource.casquatch</groupId>
            <artifactId>casquatch-driver</artifactId>
            <version>$${"{"}casquatch.version}</version>
        </dependency>
        <dependency>
            <groupId>com.tmobile.opensource.casquatch</groupId>
            <artifactId>casquatch-driver-tests</artifactId>
            <version>$${"{"}casquatch.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>$${"{"}maven-compiler.version}</version>
                <configuration>
                    <source>$${"{"}java.version}</source>
                    <target>$${"{"}java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>com.tmobile.opensource.casquatch</groupId>
                            <artifactId>casquatch-driver-processor</artifactId>
                            <version>$${"{"}casquatch.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
