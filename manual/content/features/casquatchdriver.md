---
title: "Configuration - Driver"
---

## Concept
This an extension of [Reference Configuration](https://docs.datastax.com/en/developer/java-driver/4.0/manual/core/configuration/reference/). This page will highlight a subset of the properties as well as all non-default values. Please use the reference page for further details.

## Basic Properties
| Property | Default | Description |
| ---|---|---|
| casquatch.basic.contact-points | 127.0.0.1:9042 | A list of contact point ips and ports. If providing in properties file | 
| casquatch.basic.session-keyspace | | Keyspace for the connection |
| casquatch.basic.load-balancing-policy.local-datacenter | | local datacenter for connect |
| casquatch.advanced.auth-provider.username | cassandra | Username for connection |
| casquatch.advanced.auth-provider.password | cassandra | Password for connection |

## Failover Policy
| Property | Default | Description |
| ---|---|---|
| casquatch.failover-policy.class | DefaultFailoverPolicy | Provides a failover policy to fail to a second profile. |
| casquatch.failover-policy.profile | | Name of profile to failover to in the vent of failure |
See [Failover Policy]({{< ref "failover.md" >}}) for more information

## Query Options
| Property | Default | Description |
| ---|---|---|
| casquatch.query-options.allow-non-primary-keys | false | Default to disallow queries with non primary keys. |
| casquatch.query-options.null-saving-strategy | DO_NOT_SET | Default of whether to persist nulls |
| casquatch.query-options.limit | 10 | Default row limit when doing a getAll |
| casquatch.query-options.profile | | Default execution profile |
| casquatch.solr-query-options.allow-non-primary-keys | true | Default to allow queries with non primary keys. |
| casquatch.solr-query-options.null-saving-strategy | DO_NOT_SET | Default of whether to persist nulls |
| casquatch.solr-query-options.limit | 10 | Default row limit when doing a getAll |
| casquatch.solr-query-options.profile | | Default execution profile |
See [Query Options]({{< ref "queryoptions.md" >}}) for more information

## Advanced Properties
| Property | Default | Description |
| ---|---|---|
| casquatch.basic.request.timeout | 500 milliseconds | Set the request time out duration |
| casquatch.basic.request.consistency | LOCAL_QUORUM | Default consistency level for queries |
| casquatch.basic.request.page-size | | Default page size |
| casquatch.basic.load-balancing-policy.class | | Class to provide a custom load balancer policy |
| casquatch.basic.load-balancing-policy.filter.class | | Class to provide a custom filtering policy |
| casquatch.max-requests-per-connection | 1024 | Maximum number of requests per connection |
| casquatch.advanced.connection.init-query-timeout | 12000 | How long to wait for a heartbeat |
| casquatch.advanced.connection.pool.local.size | 3 | Size of the local connection pool |
| casquatch.advanced.reconnect-on-init | true | Attempt reconnect if all contact points are unavailable |
| casquatch.advanced.reconnection-policy.class | ExponentialReconnectionPolicy | Reconnection policy to use | 
| casquatch.advanced.reconnection-policy.base-delay | 500 milliseconds | Delay between reconnections |
| casquatch.advanced.reconnection-policy.max-delay | 300 seconds | Maximum delay |
| casquatch.advanced.retry-policy.class | DefaultRetryPolicy | Reconnection policy to use |
| casquatch.advanced.speculative-execution-policy.class | ConstantSpeculativeExecutionPolicy | Speculative execution policy to use | 
| casquatch.advanced.speculative-execution-policy.max-executions = 2 | Maximum number of executions | 
| casquatch.advanced.speculative-execution-policy.delay = 500 milliseconds | Delay before reexecuting the query |
| casquatch.advanced.auth-provider.class | PlainTextAuthProvider | Authentication provider |
| casquatch.ssl-engine-factory.class | DefaultSslEngineFactory | SSL Class |
| casquatch.ssl-engine-factory.cipher-suites | [ "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA" ] | Default cipher suites |
| casquatch.ssl-engine-factory.hostname-validation | true | Hostname validation |
| casquatch.ssl-engine-factory.truststore-path | | Path to truststore |
| casquatch.ssl-engine-factory.truststore-password | | Truststore password |
| casquatch.ssl-engine-factory.keystore-path | | Path to Keystore |
| casquatch.ssl-engine-factory.keystore-password | | Keystore password |
| casquatch.profiles.ddl.basic.request.timeout | 2 seconds | Provides a DDL profile with greater timeout |

## Debugging
To enable debug logging and dump configured properties to the log, please enable the following:
com.tmobile.opensource.casquatch.ConfigLoader=DEBUG