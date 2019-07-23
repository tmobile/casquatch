---
title: "Load Test"
---

## Overview
This is an example project that generates random data then writes, reads, and compares the data. It is a command line application that does not utilize spring

## Github: [springrest](https://github.com/tmobile/casquatch/tree/master/casquatch-examples/loadtest)
## Schema
```
CREATE KEYSPACE loadtest WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE loadtest.table_name (
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
        session-keyspace = loadtest
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

loadtest {
    doRead=true
    doWrite=true
    doCheck=true
    create=false
    delay=1
    loops=1
    entities=[TableName]
    keyspace=${casquatch.basic.session-keyspace}
    datacenter=${casquatch.basic.load-balancing-policy.local-datacenter}
}

```

