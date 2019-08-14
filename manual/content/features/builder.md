---
title: "Builder Configuration"
---

## Concept
[CasquatchDao]({{% api CasquatchDao %}}) provides a [CasquatchDaoBuilder]({{% api CasquatchDaoBuilder %}}) via a static [CasquatchDao.builder()]({{% api "CasquatchDao" "builder--" %}}) method. The builder is designed to include all configuration properties converted from . format to method names as follows:

{{< highlight plaintext >}}
casquatch.basic.session.keyspace
{{< /highlight >}}

becomes
{{< highlight java >}}
builder.withBasicSessionKeyspace(String keyspace);
{{< /highlight >}}


## Examples

### Basic Driver
{{< highlight java >}}
CasquatchDao casquatchDao = 
    CasquatchDao.builder()
        .withBasicContactPoints("127.0.0.1:9042")
        .withBasicLoadBalancingPolicyLocalDatacenter("DC1")
        .withBasicSessionKeyspace("demo")
        .build();
{{< /highlight >}}

### Basic Driver with Authentication
{{< highlight java >}}
CasquatchDao casquatchDao = 
    CasquatchDao.builder()
        .withBasicContactPoints("127.0.0.1:9042")
        .withBasicLoadBalancingPolicyLocalDatacenter("DC1")
        .withBasicSessionKeyspace("demo")
        .withAdvancedAuthProviderUsername("cassandra")
        .withAdvancedAuthProviderPassword("cassandra")
        .build();
{{< /highlight >}}

### Advanced Example
{{< highlight java >}}
CasquatchDao casquatchDao = 
    CasquatchDao.builder()
        .withBasicContactPoints("127.0.0.1:9042")
        .withBasicLoadBalancingPolicyLocalDatacenter("DC1")
        .withBasicSessionKeyspace("demo")
        .withAdvancedAuthProviderUsername("cassandra")
        .withAdvancedAuthProviderPassword("cassandra")
        .startProfile("remote")
        .withBasicLoadBalancingPolicyLocalDatacenter("DC2")
        .endProfile()
        .withFailoverPolicyProfile("remote")
        .withBasicRequestTimeout("2 seconds")
        .build();
{{< /highlight >}}