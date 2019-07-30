CREATE KEYSPACE springconfigserver WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE springconfigserver.configuration (
    application text,
    profile text,
    label text,
    key text,
    value text,
    PRIMARY KEY ((application, profile), label, key)
) WITH CLUSTERING ORDER BY (label ASC, key ASC);