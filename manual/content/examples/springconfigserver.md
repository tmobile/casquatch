---
title: "Spring Config Server"
---

## Overview
This is a project that implements [Spring Config Server](https://spring.io/guides/gs/centralized-configuration/) using the Casquatch Driver. Entity was generated via [casquatch-generator]()

## Github: [springconfigserver](https://github.com/tmobile/casquatch/tree/master/casquatch-examples/springconfigserver)

## Schema
```
CREATE KEYSPACE springconfigserver WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE springconfigserver.configuration (
    application text,
    profile text,
    label text,
    key text,
    value text,
    PRIMARY KEY ((application, profile), label, key)
) WITH CLUSTERING ORDER BY (label ASC, key ASC);
EOF
```

## Configuration
```
casquatch {
    basic {
        contact-points = [
            "127.0.0.1:9071"
        ]
        session-keyspace = springrest
        load-balancing-policy.local-datacenter=datacenter1
    }
    generator {
        console=false
        file=true
        overwrite=true
        outputFolder=.
        packageName=com.tmobile.opensource.casquatch.examples
        createPackage=false
        contactPoints=${casquatch.basic.contact-points}
        keyspace=${casquatch.basic.session-keyspace}
        datacenter=${casquatch.basic.load-balancing-policy.local-datacenter}
    }
}

server.servlet.context-path=/${casquatch.basic.session-keyspace}
```

