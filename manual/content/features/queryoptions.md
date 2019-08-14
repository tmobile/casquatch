---
title: "Query Options"
---

## Concept
In order to provide a fluid model of specifying QueryOptions, a QueryOptions object is provided which is immutable and chainable. See [Query Options]({{%api QueryOptions %}}) for more information. All CasquatchDao query APIs are overloaded allowing for this object to be passed.

## Options
* All Columns - Indicates if a query should be built referencing only the keys or all columns. If all columns is used and the query method does not support non key columns then this will reuslt in an exception.
* Consistency Level - Specify a per query consistency level
* Limit - Specify the number of rows to return
* TTL - Set a TTL for the query
* Profile - Excecution profile to use. See (Execution Profiles)[https://docs.datastax.com/en/developer/java-driver/4.1/manual/core/configuration/#execution-profiles] for more information
