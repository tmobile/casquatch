package com.tmobile.opensource.casquatch.examples.loadtest;


import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
import com.tmobile.opensource.casquatch.annotation.ClusteringColumn;
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
import java.lang.Integer;
import java.lang.String;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.text.TextStringBuilder;

@CasquatchEntity
@Getter @Setter @NoArgsConstructor
public class TableName extends AbstractCasquatchEntity {
    @PartitionKey
    private Integer keyOne;

    @ClusteringColumn(1)
    private Integer keyTwo;

    private String colOne;
    private String colTwo;

    /**
    * Generated: Initialize with Partition Keys
        * @param keyOne Partition Key Named key_one
    */
    public TableName(Integer keyOne) {
        this.setKeyOne(keyOne);
    }

    /**
    * Generated: Initialize with Partition and Clustering Keys
        * @param keyOne Partition Key Named key_one
        * @param keyTwo Clustering Key Named key_two
    */
    public TableName(Integer keyOne,Integer keyTwo) {
        this.setKeyOne(keyOne);
        this.setKeyTwo(keyTwo);
    }

    /**
     * Generated: Instance of object containing primary keys only
     */
    @CasquatchIgnore
    public TableName keys() {
        TableName tableName = new TableName();
        tableName.setKeyOne(this.getKeyOne());
        tableName.setKeyTwo(this.getKeyTwo());
        return tableName;
    }

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        TextStringBuilder ddl = new TextStringBuilder();
        ddl.appendln("CREATE TABLE \"table_name\" ( \"key_one\" int, \"key_two\" int, \"col_one\" text, \"col_two\" text, PRIMARY KEY (\"key_one\", \"key_two\") ) WITH bloom_filter_fp_chance = 0.01 AND caching = {'keys':'ALL','rows_per_partition':'NONE'} AND comment = '' AND compaction = {'class':'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy','max_threshold':'32','min_threshold':'4'} AND compression = {'chunk_length_in_kb':'64','class':'org.apache.cassandra.io.compress.LZ4Compressor'} AND crc_check_chance = 1.0 AND dclocal_read_repair_chance = 0.1 AND default_time_to_live = 0 AND extensions = {} AND gc_grace_seconds = 864000 AND max_index_interval = 2048 AND memtable_flush_period_in_ms = 0 AND min_index_interval = 128 AND read_repair_chance = 0.0 AND speculative_retry = '99PERCENTILE';");
        return ddl.toString();
    }
}

