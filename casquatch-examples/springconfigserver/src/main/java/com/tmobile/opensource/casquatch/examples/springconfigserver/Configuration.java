package com.tmobile.opensource.casquatch.examples.springconfigserver;


import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
import com.tmobile.opensource.casquatch.annotation.ClusteringColumn;
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
import java.lang.String;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.text.TextStringBuilder;

@CasquatchEntity
@Getter @Setter @NoArgsConstructor
public class Configuration extends AbstractCasquatchEntity {
    @PartitionKey
    private String application;
    @PartitionKey
    private String profile;

    @ClusteringColumn(1)
    private String label;
    @ClusteringColumn(2)
    private String key;

    private String value;

    /**
    * Generated: Initialize with Partition Keys
        * @param application Partition Key Named application
        * @param profile Partition Key Named profile
    */
    public Configuration(String application,String profile) {
        this.setApplication(application);
        this.setProfile(profile);
    }

    /**
    * Generated: Initialize with Partition and Clustering Keys
        * @param application Partition Key Named application
        * @param profile Partition Key Named profile
        * @param label Clustering Key Named label
        * @param key Clustering Key Named key
    */
    public Configuration(String application,String profile,String label,String key) {
        this.setApplication(application);
        this.setProfile(profile);
        this.setLabel(label);
        this.setKey(key);
    }

    /**
     * Generated: Instance of object containing primary keys only
     */
    @CasquatchIgnore
    public Configuration keys() {
        Configuration configuration = new Configuration();
        configuration.setApplication(this.getApplication());
        configuration.setProfile(this.getProfile());
        configuration.setLabel(this.getLabel());
        configuration.setKey(this.getKey());
        return configuration;
    }

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        TextStringBuilder ddl = new TextStringBuilder();
        ddl.appendln("CREATE TABLE \"configuration\" ( \"application\" text, \"profile\" text, \"label\" text, \"key\" text, \"value\" text, PRIMARY KEY ((\"application\", \"profile\"), \"label\", \"key\") ) WITH bloom_filter_fp_chance = 0.01 AND caching = {'keys':'ALL','rows_per_partition':'NONE'} AND comment = '' AND compaction = {'class':'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy','max_threshold':'32','min_threshold':'4'} AND compression = {'chunk_length_in_kb':'64','class':'org.apache.cassandra.io.compress.LZ4Compressor'} AND crc_check_chance = 1.0 AND dclocal_read_repair_chance = 0.1 AND default_time_to_live = 0 AND extensions = {} AND gc_grace_seconds = 864000 AND max_index_interval = 2048 AND memtable_flush_period_in_ms = 0 AND min_index_interval = 128 AND read_repair_chance = 0.0 AND speculative_retry = '99PERCENTILE';");
        return ddl.toString();
    }
}

