/*
 * Copyright 2018 T-Mobile US, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tmobile.opensource.casquatch.models;


import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.TextStringBuilder;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CasquatchEntity(table = "system.local")
@Getter @Setter @NoArgsConstructor
public class NodeMetaData extends AbstractCasquatchEntity {

    @PartitionKey
    private String key;

    private String clusterName;
    private String rack;
    private Map<UUID,ByteBuffer> truncatedAt;
    private String cqlVersion;
    private Set<String> workloads;
    private String workload;
    private String dataCenter;
    private String serverId;
    private InetAddress rpcAddress;
    private InetAddress broadcastAddress;
    private Boolean graph;
    private UUID hostId;
    private UUID schemaVersion;
    private String bootstrapped;
    private String nativeProtocolVersion;
    private String dseVersion;
    private String partitioner;
    private InetAddress listenAddress;
    private Set<String> tokens;
    private String releaseVersion;
    private Integer gossipGeneration;
    private String thriftVersion;

    /**
    * Generated: Initialize with Partition Keys
        * @param key Partition Key Named key
    */
    public NodeMetaData(String key) {
        this.setKey(key);
    }


    /**
     * Generated: Instance of object containing primary keys only
     */
    @CasquatchIgnore
    public NodeMetaData keys() {
        NodeMetaData local = new NodeMetaData();
        local.setKey(this.getKey());
        return local;
    }

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        TextStringBuilder ddl = new TextStringBuilder();
        ddl.appendln("CREATE TABLE \"local\" ( \"key\" text, \"bootstrapped\" text, \"broadcast_address\" inet, \"cluster_name\" text, \"cql_version\" text, \"data_center\" text, \"dse_version\" text, \"gossip_generation\" int, \"graph\" boolean, \"host_id\" uuid, \"listen_address\" inet, \"native_protocol_version\" text, \"partitioner\" text, \"rack\" text, \"release_version\" text, \"rpc_address\" inet, \"schema_version\" uuid, \"server_id\" text, \"thrift_version\" text, \"tokens\" set<text>, \"truncated_at\" map<uuid, blob>, \"workload\" text, \"workloads\" frozen<set<text>>, PRIMARY KEY (\"key\") ) WITH bloom_filter_fp_chance = 0.01 AND caching = {'keys':'ALL','rows_per_partition':'NONE'} AND comment = 'information about the local node' AND compaction = {'class':'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy','max_threshold':'32','min_threshold':'4'} AND compression = {'chunk_length_in_kb':'64','class':'org.apache.cassandra.io.compress.LZ4Compressor'} AND crc_check_chance = 1.0 AND dclocal_read_repair_chance = 0.0 AND default_time_to_live = 0 AND extensions = {} AND gc_grace_seconds = 0 AND max_index_interval = 2048 AND memtable_flush_period_in_ms = 3600000 AND min_index_interval = 128 AND read_repair_chance = 0.0 AND speculative_retry = '99PERCENTILE';");
        return ddl.toString();
    }
}

