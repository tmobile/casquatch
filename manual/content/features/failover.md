---
title: "Failover"
---

## Concept
Casquatch is designed to support failing over database queries from one datacenter to another. This generally causes increased latency and weaker consistency in exchange for improved availability.

## Method
Casquatch APIs are designed to build statements which then get passed to [execute]({{% api "CasquatchDao" "execute-com.datastax.oss.driver.api.core.cql.Statement-"%}}) or [executeASync]({{% api "CasquatchDao" "executeASync-com.datastax.oss.driver.api.core.cql.Statement-"%}}) as appropriate. If an exception is caught then it is passed to the class defined by [casquatch.failover-policy.class]({{< ref "casquatchdriver.md" >}}) which implements [FailoverPolicy]({{ % api "policies/FailoverPolicy" %}}). This interface takes a statement and an exception to determine if it should be replayed on the remote site. If so, then it is attempted again with the profile defined in [casquatch.failover-policy.profile]({{< ref "casquatchdriver.md" >}}). This parameter supports being nested in additional profiles allowing chained failovers as required.   