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

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.tmobile.opensource.casquatch.AbstractStatementFactory;
import com.tmobile.opensource.casquatch.QueryOptions;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;

@Slf4j
public class NodeMetaData_StatementFactory extends AbstractStatementFactory<NodeMetaData> {


    public NodeMetaData_StatementFactory(CqlSession session) {
        super(NodeMetaData.class,session);
    }

    @Override
    protected Select selectWhereObject(Select select, NodeMetaData nodeMetaData, QueryOptions options) {

        if(nodeMetaData.getKey()!=null) {
            select=select.whereColumn("key").isEqualTo(bindMarker());
        }
        if(!options.getIgnoreNonPrimaryKeys()) {
            if(nodeMetaData.getGossipGeneration()!=null) {
                select=select.whereColumn("gossip_generation").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getRack()!=null) {
                select=select.whereColumn("rack").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getSchemaVersion()!=null) {
                select=select.whereColumn("schema_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getThriftVersion()!=null) {
                select=select.whereColumn("thrift_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getDataCenter()!=null) {
                select=select.whereColumn("data_center").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getRpcAddress()!=null) {
                select=select.whereColumn("rpc_address").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getReleaseVersion()!=null) {
                select=select.whereColumn("release_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getWorkloads()!=null) {
                select=select.whereColumn("workloads").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getWorkload()!=null) {
                select=select.whereColumn("workload").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getHostId()!=null) {
                select=select.whereColumn("host_id").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getServerId()!=null) {
                select=select.whereColumn("server_id").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getGraph()!=null) {
                select=select.whereColumn("graph").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getTruncatedAt()!=null) {
                select=select.whereColumn("truncated_at").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getBroadcastAddress()!=null) {
                select=select.whereColumn("broadcast_address").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getDseVersion()!=null) {
                select=select.whereColumn("dse_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getCqlVersion()!=null) {
                select=select.whereColumn("cql_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getBootstrapped()!=null) {
                select=select.whereColumn("bootstrapped").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getNativeProtocolVersion()!=null) {
                select=select.whereColumn("native_protocol_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getListenAddress()!=null) {
                select=select.whereColumn("listen_address").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getClusterName()!=null) {
                select=select.whereColumn("cluster_name").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getPartitioner()!=null) {
                select=select.whereColumn("partitioner").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getTokens()!=null) {
                select=select.whereColumn("tokens").isEqualTo(bindMarker());
            }
        }
        return select;
    }

    @Override
    protected RegularInsert insertObject(NodeMetaData nodeMetaData, QueryOptions options) {
        RegularInsert insert=null;
        if(nodeMetaData.getKey()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("key",bindMarker());
        }
        if(nodeMetaData.getGossipGeneration()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("gossip_generation",bindMarker());
        }
        if(nodeMetaData.getRack()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("rack",bindMarker());
        }
        if(nodeMetaData.getSchemaVersion()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("schema_version",bindMarker());
        }
        if(nodeMetaData.getThriftVersion()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("thrift_version",bindMarker());
        }
        if(nodeMetaData.getDataCenter()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("data_center",bindMarker());
        }
        if(nodeMetaData.getRpcAddress()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("rpc_address",bindMarker());
        }
        if(nodeMetaData.getReleaseVersion()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("release_version",bindMarker());
        }
        if(nodeMetaData.getWorkloads()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("workloads",bindMarker());
        }
        if(nodeMetaData.getWorkload()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("workload",bindMarker());
        }
        if(nodeMetaData.getHostId()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("host_id",bindMarker());
        }
        if(nodeMetaData.getServerId()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("server_id",bindMarker());
        }
        if(nodeMetaData.getGraph()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("graph",bindMarker());
        }
        if(nodeMetaData.getTruncatedAt()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("truncated_at",bindMarker());
        }
        if(nodeMetaData.getBroadcastAddress()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("broadcast_address",bindMarker());
        }
        if(nodeMetaData.getDseVersion()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("dse_version",bindMarker());
        }
        if(nodeMetaData.getCqlVersion()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("cql_version",bindMarker());
        }
        if(nodeMetaData.getBootstrapped()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("bootstrapped",bindMarker());
        }
        if(nodeMetaData.getNativeProtocolVersion()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("native_protocol_version",bindMarker());
        }
        if(nodeMetaData.getListenAddress()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("listen_address",bindMarker());
        }
        if(nodeMetaData.getClusterName()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("cluster_name",bindMarker());
        }
        if(nodeMetaData.getPartitioner()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("partitioner",bindMarker());
        }
        if(nodeMetaData.getTokens()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("tokens",bindMarker());
        }
        return insert;
    }

    @Override
    protected Delete deleteObject(NodeMetaData nodeMetaData, QueryOptions options) {
        Delete delete=null;

        if(nodeMetaData.getKey()!=null) {
            delete=(delete==null?deleteStart:delete).whereColumn("key").isEqualTo(bindMarker());
       }
        if(!options.getIgnoreNonPrimaryKeys()) {
            if(nodeMetaData.getGossipGeneration()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("gossip_generation").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getRack()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("rack").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getSchemaVersion()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("schema_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getThriftVersion()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("thrift_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getDataCenter()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("data_center").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getRpcAddress()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("rpc_address").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getReleaseVersion()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("release_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getWorkloads()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("workloads").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getWorkload()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("workload").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getHostId()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("host_id").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getServerId()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("server_id").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getGraph()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("graph").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getTruncatedAt()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("truncated_at").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getBroadcastAddress()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("broadcast_address").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getDseVersion()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("dse_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getCqlVersion()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("cql_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getBootstrapped()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("bootstrapped").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getNativeProtocolVersion()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("native_protocol_version").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getListenAddress()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("listen_address").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getClusterName()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("cluster_name").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getPartitioner()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("partitioner").isEqualTo(bindMarker());
            }
            if(nodeMetaData.getTokens()!=null) {
                delete=(delete==null?deleteStart:delete).whereColumn("tokens").isEqualTo(bindMarker());
            }
        }
        return delete;
    }

    @Override
    public CqlIdentifier getTableName() {
        return CqlIdentifier.fromCql("local");
    }

    @Override
    protected BoundStatementBuilder bindObject(BoundStatementBuilder boundStatementBuilder, NodeMetaData nodeMetaData, QueryOptions options) {
        if(nodeMetaData.getKey()!=null || options.getPersistNulls()) {
            boundStatementBuilder = boundStatementBuilder.set("key", nodeMetaData.getKey(), String.class);
        }
        if(!options.getIgnoreNonPrimaryKeys()) {
            if(nodeMetaData.getGossipGeneration()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("gossip_generation", nodeMetaData.getGossipGeneration(), Integer.class);
            }
            if(nodeMetaData.getRack()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("rack", nodeMetaData.getRack(), String.class);
            }
            if(nodeMetaData.getSchemaVersion()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("schema_version", nodeMetaData.getSchemaVersion(), UUID.class);
            }
            if(nodeMetaData.getThriftVersion()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("thrift_version", nodeMetaData.getThriftVersion(), String.class);
            }
            if(nodeMetaData.getDataCenter()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("data_center", nodeMetaData.getDataCenter(), String.class);
            }
            if(nodeMetaData.getRpcAddress()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("rpc_address", nodeMetaData.getRpcAddress(), InetAddress.class);
            }
            if(nodeMetaData.getReleaseVersion()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("release_version", nodeMetaData.getReleaseVersion(), String.class);
            }
            if(nodeMetaData.getWorkloads()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("workloads", nodeMetaData.getWorkloads(), Set.class);
            }
            if(nodeMetaData.getWorkload()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("workload", nodeMetaData.getWorkload(), String.class);
            }
            if(nodeMetaData.getHostId()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("host_id", nodeMetaData.getHostId(), UUID.class);
            }
            if(nodeMetaData.getServerId()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("server_id", nodeMetaData.getServerId(), String.class);
            }
            if(nodeMetaData.getGraph()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("graph", nodeMetaData.getGraph(), Boolean.class);
            }
            if(nodeMetaData.getTruncatedAt()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("truncated_at", nodeMetaData.getTruncatedAt(), Map.class);
            }
            if(nodeMetaData.getBroadcastAddress()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("broadcast_address", nodeMetaData.getBroadcastAddress(), InetAddress.class);
            }
            if(nodeMetaData.getDseVersion()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("dse_version", nodeMetaData.getDseVersion(), String.class);
            }
            if(nodeMetaData.getCqlVersion()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("cql_version", nodeMetaData.getCqlVersion(), String.class);
            }
            if(nodeMetaData.getBootstrapped()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("bootstrapped", nodeMetaData.getBootstrapped(), String.class);
            }
            if(nodeMetaData.getNativeProtocolVersion()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("native_protocol_version", nodeMetaData.getNativeProtocolVersion(), String.class);
            }
            if(nodeMetaData.getListenAddress()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("listen_address", nodeMetaData.getListenAddress(), InetAddress.class);
            }
            if(nodeMetaData.getClusterName()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("cluster_name", nodeMetaData.getClusterName(), String.class);
            }
            if(nodeMetaData.getPartitioner()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("partitioner", nodeMetaData.getPartitioner(), String.class);
            }
            if(nodeMetaData.getTokens()!=null || options.getPersistNulls()) {
                boundStatementBuilder = boundStatementBuilder.set("tokens", nodeMetaData.getTokens(), Set.class);
            }
        }
        return boundStatementBuilder;
    }

    @Override
    public NodeMetaData map(GettableByName source) {
        NodeMetaData nodeMetaData = new NodeMetaData();
        try {
            nodeMetaData.setKey(source.get("key",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setGossipGeneration(source.get("gossip_generation",Integer.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setRack(source.get("rack",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setSchemaVersion(source.get("schema_version",UUID.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setThriftVersion(source.get("thrift_version",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setDataCenter(source.get("data_center",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setRpcAddress(source.get("rpc_address",InetAddress.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setReleaseVersion(source.get("release_version",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setWorkloads(source.get("workloads",Set.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setWorkload(source.get("workload",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setHostId(source.get("host_id",UUID.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setServerId(source.get("server_id",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setGraph(source.get("graph",Boolean.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setTruncatedAt(source.get("truncated_at",Map.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setBroadcastAddress(source.get("broadcast_address",InetAddress.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setDseVersion(source.get("dse_version",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setCqlVersion(source.get("cql_version",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setBootstrapped(source.get("bootstrapped",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setNativeProtocolVersion(source.get("native_protocol_version",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setListenAddress(source.get("listen_address",InetAddress.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setClusterName(source.get("cluster_name",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setPartitioner(source.get("partitioner",String.class));
        }
        catch (IllegalArgumentException e) {
        }
        try {
            nodeMetaData.setTokens(source.get("tokens",Set.class));
        }
        catch (IllegalArgumentException e) {
        }
        return nodeMetaData;
    }

}
