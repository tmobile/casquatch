---
title: "Concepts"
weight: 14
---
## CasquatchDao
[CasquatchDao]({{% api CasquatchDao %}}) is your main interface to the database and provides an extensive [api]({{< ref "api.md" >}}). This can be configured through a [Builder]({{< ref "builder.md" >}}), Autowired with [Spring]({{< ref "configurespring.md" >}}), or loaded through a [configuration file]({{< ref "casquatchdriver.md" >}}).

## Casquatch Entity
The CasquatchEntity corresponds directly to a table. Fields and naming are translated through [CasquatchNamingConvention]({{% api CasquatchNamingConvention %}}) to CQL equivalents. At minimum an entity will look like the following:
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

The entity must contain [@CasquatchEntity]({{% api "annotation/CasquatchEntity" %}}), a field annotated with [@PartitionKey]({{% api "annotation/PartitionKey" %}}), Setters for each field, and a No Args Constructor.

## Statement Factory
Each entity annotation with [@CasquatchEntity]({{% api "annotation/CasquatchEntity" %}}) creates an implementation of [AbstractStatementFactory]({{% api AbstractStatementFactory %}}) to provide the generation of appropriate prepared statements as well as the mappings between the statements and the objects. 
