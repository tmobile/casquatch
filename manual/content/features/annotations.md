---
title: "Annotations"
---

## Concept
Annotation processing allows the compile to build class files based on provided annotations. This allows for clean consistent generated code to be built for each entity as required. 

## Override
The generators are defined such that any generated source will first check for the existence of the same class file. Thus you can copy a file out from target/generated-sources and place it within your project to customize and use as desired.

## Annotations
### [CasquatchEntity]({{% api "annotation/CasquatchEntity" %}})
This is the core annotation for referencing a Casquatch Entity. This is used to trigger the creation of the implementation for [AbstractStatementFactory]({{% api "AbstractStatementFactory" %}}) used by [CasquatchDao]({{% api "CasquatchDao" %}}). This annotation will typically be added only on Entities which should be created by [Code Generator]({{% ref codegenerator %}})

### [CasquatchType]({{% api "annotation/CasquatchType" %}})
This is the annotation for referencing a Casquatch Type which represents a CQL User Defined Type. This is used to trigger the creation of the implementation for [AbstractTypeFactory]({{% api "AbstractTypeFactory" %}}) used by [AbstractStatementFactory]({{% api "AbstractStatementFactory" %}}). This annotation will typically be added only on Types which should be created by [Code Generator]({{% ref codegenerator %}})

### [CasquatchSpring]({{% api "annotation/CasquatchSpring" %}})
This annotation must be placed on the main application class within a Spring project to integrate Casquatch as it imports [CasquatchSpringBeans]({{% api CasquatchSpringBeans %}})

## Maven Configuration
Configure the pom.xml to reference casquatch-driver-processor for annotation processing
{{< highlight xml >}}
<build>
    <plugins>
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
    </plugins>
</build>
{{< /highlight >}}
