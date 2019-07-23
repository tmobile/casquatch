---
title: "Summary"
weight: 21
---
* Built on top of Datastax Driver utilizing supported APIs
* Configurable [Failover Policy]({{< ref "failover.md" >}})
* Entity [Code Generator]({{< ref "codegenerator.md" >}}) from database metadata
* [Data Cache]({{< ref "drivercache.md" >}})
* [Object Based API]({{< ref "api.md" >}})
* Optional [Spring Integration]({{< ref "configurespring.md" >}})
* Optional [Rest API]({{< ref "restapi.md" >}})
* Uses prepared statements for performance gains and type safe queries
* Significantly reduces code development effort and reduces code duplication
* Customizations available through standard [application.properties]({{< ref "casquatchdriver.md" >}})
* Support to query [Solr]({{< ref "solr.md" >}}) via solr_query or via Object
* [Annotation Processing]({{< ref "annotations.md" >}}) to generate entity specific classes