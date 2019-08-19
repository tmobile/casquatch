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

package com.tmobile.opensource.casquatch;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.extern.slf4j.Slf4j;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import java.util.List;
import java.util.Map;

/**
 * Extension of CasquatchDaoBuilder to add a few test functions
 */
@Slf4j
public class CasquatchTestDaoBuilder extends CasquatchDaoBuilder {

    /**
     * Create DAO with embedded cassandra
     * @return builder using embedded cassandra
     */
    public CasquatchTestDaoBuilder withEmbedded() {
        try {

            EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE, EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT);
            log.info("Embedded Cassandra Started");
            return (CasquatchTestDaoBuilder) this.withBasicContactPoints(EmbeddedCassandraServerHelper.getHost()+":"+EmbeddedCassandraServerHelper.getNativeTransportPort())
                            .withBasicLoadBalancingPolicyLocalDatacenter("datacenter1")
                            .withBasicRequestConsistency("LOCAL_ONE");
        }
        catch(Exception e) {
            throw new DriverException(e);
        }
    }

    /**
     * Run DDL in the DAO before building
     * @param ddl cql to run
     * @return builder after running ddl
     */
    public CasquatchTestDaoBuilder withDDL(String ddl) {
        CqlSession session = this.session();
        try {
            for(String query : ddl.split(System.lineSeparator())) {
                log.info("Executing DDL: {}",query);
                session.execute(SimpleStatement.builder(query).setExecutionProfileName("ddl").build());
            }
            session.close();
        }
        catch(com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException e) {
            log.warn("DDL Exception: {}",e.getMessage());
        }
        catch(com.datastax.oss.driver.api.core.servererrors.InvalidQueryException e) {
            log.warn("DDL Exception: {}",e.getMessage());
        }
        catch(Exception e) {
            log.warn("DDL Exception: {}",e.getMessage());
            throw(e);
        }
        return this;
    }

    /**
     * Creates a search index on the given table if required
     * @param table name of table
     * @return builder reference
     */
    public CasquatchTestDaoBuilder withSolrIndex(String table) {
        CasquatchDao casquatchDao = this.build();
        if(casquatchDao.checkFeature(CasquatchDao.FEATURES.SOLR)) {
            this.withDDL(String.format("CREATE SEARCH INDEX ON %s",table));
        }
        casquatchDao.close();
        return this;
    }

    /**
     * Use and create keyspace if required
     * @param keyspace name of keyspace
     * @return builder with keyspace
     */
    public CasquatchTestDaoBuilder withTestKeyspace(String keyspace) {
        CqlSession session = this.session("system");
        try {
            session.execute(SimpleStatement.builder(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true;",keyspace)).setExecutionProfileName("ddl").build());
            session.close();
            log.info("Keyspace Created: {}",keyspace);
        }
        catch(Exception e) {
            log.error("Error running withTestKeyspace",e);
        }
        return (CasquatchTestDaoBuilder) this.withBasicSessionKeyspace(keyspace);
    }

    /**
     * Converts config to system properties to be grabbed by {@link org.springframework.beans.factory.annotation.Autowired}
     */
    public void buildSpring() {
        for(Map.Entry<String,Object> entry : this.configMap.entrySet()) {
            if(entry.getValue() instanceof String) {
                System.setProperty("casquatch." + entry.getKey(), entry.getValue().toString());
            }
            else {
                List<String> entryList = (List<String>) entry.getValue();
                for(int i=0;i<entryList.size();i++) {
                    System.setProperty("casquatch." + entry.getKey()+"."+i, entryList.get(i));
                }
            }
        }
    }
}

