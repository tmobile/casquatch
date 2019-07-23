---
title: "Solr"
---

## Concept
Datastax solr queries are available with multiple interfaces. This requires that solr be set up and configured on the Cassandra database server.

## JSON (Requires DSE 5.1+)
Query by JSON with the [getAllBySolr(class,solrQuery)]({{% api "CasquatchDao" "getAllBySolr-java.lang.Class-java.lang.String-" %}}), For details on the format of the solrQuery, please consult [DSE 5.1 - Search index filter syntax](https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax) for details.

## Object (Requires DSE 6.0+)
Adds Query by Object with [getAllBySolr(class,object)]({{% api "CasquatchDao" "getAllBySolr-java.lang.Class-T-" %}}). See [DSE 6.0 - Search index filter syntax](https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/siQuerySyntax.html) for details
