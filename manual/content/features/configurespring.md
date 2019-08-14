---
title: "Configure Spring"
---

## Base Configuration
First configure according to [Maven Configuration]({{< ref "mavenconfiguration.md" >}})

## Add dependency for Casquatch-Driver-Spring
{{< highlight xml >}}
<dependency>
    <groupId>com.tmobile.opensource.casquatch</groupId>
    <artifactId>casquatch-driver-spring</artifactId>
    <version>${casquatch.version}</version>
</dependency>
{{< /highlight >}}
  
## Add Annotation
Find @SpringBootApplication and add this line:
{{< highlight java >}}
@CasquatchSpring
{{< /highlight >}}

## Use Autowired CasquatchDao bean where needed
{{< highlight java >}}
@Autowired
private CasquatchDao db;
{{< /highlight >}}