---
title: "Spring Rest"
---

## Overview
This is a simple project that utilizes Spring and Casquatch to provide a Rest API for a given schema. Entity was generated via the [Code Generator]({{< ref "features/codegenerator.md" >}})

## Github: [springrest](https://github.com/tmobile/casquatch/tree/master/casquatch-examples/springrest)

## Schema
```
CREATE KEYSPACE springrest WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE springrest.table_name (
    key_one int,
    key_two int,
    col_one text,
    col_two text,
    PRIMARY KEY (key_one, key_two)
);
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

