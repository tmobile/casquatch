---
title: "Maven Configuration"
---

## Add version property
{{< highlight xml >}}
<casquatch.version>X.Y-RELEASE</casquatch.version>
{{< /highlight >}}

## Add dependency for Driver
{{< highlight xml >}}
  <dependency>
    <groupId>com.tmobile.opensource.casquatch</groupId>
    <artifactId>casquatch-driver</artifactId>
    <version>${casquatch.version}</version>
  </dependency>
{{< /highlight >}}

## Optional: Add test dependency for Driver
{{< highlight xml >}}
  <dependency>
    <groupId>com.tmobile.opensource.casquatch</groupId>
    <artifactId>casquatch-driver-tests</artifactId>
    <version>${casquatch.version}</version>
    <scope>test</scope>
  </dependency>
{{< /highlight >}}
  
## Add annotation processing
See [Annotations]({{< ref "annotations.md" >}}) for more information
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
