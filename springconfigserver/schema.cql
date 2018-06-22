CREATE KEYSPACE springconfig WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;

CREATE TABLE springconfig.configuration (
    application text,
    profile text,
    label text,
    key text,
    value text,
    PRIMARY KEY ((application, profile), label, key)
) WITH CLUSTERING ORDER BY (label ASC, key ASC);

CREATE TABLE springconfig.driver_config (
    table_name text PRIMARY KEY,
    create_dttm timestamp,
    create_user text,
    data_center text,
    mod_dttm timestamp,
    mod_user text,
    read_consistency text,
    write_consistency text
);
