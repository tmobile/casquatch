package com.tmobile.opensource.casquatch.tests;


import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
import com.tmobile.opensource.casquatch.annotation.UDT;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.TextStringBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CasquatchEntity(generateTests = true)
@Getter @Setter @NoArgsConstructor
public class TortureTable extends AbstractCasquatchEntity {
    @PartitionKey
    private UUID id;

    private ByteBuffer colBlob;
    private Instant colTimestamp;
    private Integer colInt;
    private String colText;
    private LocalTime colTime;
    private Double colDouble;
    private InetAddress colInet;
    private Map<String,String> colMap;
    private String colVarchar;
    private Byte colTinyint;
    private BigInteger colVarint;
    private LocalDate colDate;
    private String colAscii;
    private Short colSmallint;
    private Set<String> colSet;
    private BigDecimal colDecimal;
    private Long colBigint;
    private Boolean colBoolean;
    private Float colFloat;
    private List<String> colList;
    private UUID colTimeuuid;
    private UUID colUuid;

    @UDT
    private Udt colUdt;

    /**
    * Generated: Initialize with Partition Keys
        * @param id Partition Key Named id
    */
    public TortureTable(UUID id) {
        this.setId(id);
    }


    /**
     * Generated: Instance of object containing primary keys only
     */
    @CasquatchIgnore
    public TortureTable keys() {
        TortureTable tortureTable = new TortureTable();
        tortureTable.setId(this.getId());
        return tortureTable;
    }

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        TextStringBuilder ddl = new TextStringBuilder();
        ddl.appendln(Udt.getDDL());
        ddl.appendln("CREATE TABLE \"torture_table\" ( \"id\" uuid, \"col_ascii\" ascii, \"col_bigint\" bigint, \"col_blob\" blob, \"col_boolean\" boolean, \"col_date\" date, \"col_decimal\" decimal, \"col_double\" double, \"col_float\" float, \"col_inet\" inet, \"col_int\" int, \"col_list\" list<text>, \"col_map\" map<text, text>, \"col_set\" set<text>, \"col_smallint\" smallint, \"col_text\" text, \"col_time\" time, \"col_timestamp\" timestamp, \"col_timeuuid\" timeuuid, \"col_tinyint\" tinyint, \"col_udt\" frozen<\"junittest\".\"udt\">, \"col_uuid\" uuid, \"col_varchar\" text, \"col_varint\" varint, PRIMARY KEY (\"id\") ) WITH bloom_filter_fp_chance = 0.01 AND caching = {'keys':'ALL','rows_per_partition':'NONE'} AND comment = '' AND compaction = {'class':'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy','max_threshold':'32','min_threshold':'4'} AND compression = {'chunk_length_in_kb':'64','class':'org.apache.cassandra.io.compress.LZ4Compressor'} AND crc_check_chance = 1.0 AND dclocal_read_repair_chance = 0.1 AND default_time_to_live = 0 AND extensions = {} AND gc_grace_seconds = 864000 AND max_index_interval = 2048 AND memtable_flush_period_in_ms = 0 AND min_index_interval = 128 AND read_repair_chance = 0.0 AND speculative_retry = '99PERCENTILE';");
        return ddl.toString();
    }
}

