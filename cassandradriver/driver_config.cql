CREATE TABLE driver_config (
    table_name text PRIMARY KEY,
    create_dttm timestamp,
    create_user text,
    data_center text,
    mod_dttm timestamp,
    mod_user text,
    read_consistency text,
    write_consistency text
);
