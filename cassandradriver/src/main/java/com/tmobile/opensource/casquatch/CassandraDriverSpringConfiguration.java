/* Copyright 2018 T-Mobile US, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
/**
 * Spring Configuration for autowiring of beans. Import with @Import(CassandraDriverSpringConfiguration.class) in your main Application class
 *
 * @version 1.2
 * @since   2018-04-19
 */
@Configuration
public class CassandraDriverSpringConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(CassandraDriverSpringConfiguration.class);

    @Value("${cassandraDriver.contactPoints:#{null}}") String contactPoints;
	@Value("${cassandraDriver.keyspace:#{null}}") String keyspace;
	@Value("${cassandraDriver.localDC:#{null}}") String localDC;
	@Value("${cassandraDriver.port:-1}") int port;
	@Value("${cassandraDriver.username:#{null}}") String username;
	@Value("${cassandraDriver.password:#{null}}") String password;
	@Value("${cassandraDriver.defaults.consistencyLevel:#{null}}") String defaultConsistencyLevel;
	@Value("${cassandraDriver.defaults.clusterType:#{null}}") String defaultClusterType;
	@Value("${cassandraDriver.defaults.solrDC:#{null}}") String defaultSolrDC;
	@Value("${cassandraDriver.defaults.saveNulls:#{null}}") String saveNulls;
	@Value("${cassandraDriver.useRemoteConnections:-1}") int useRemoteConnections;
	@Value("${cassandraDriver.connections.local.min:-1}") int localConnectionLimitMin;
	@Value("${cassandraDriver.connections.local.max:-1}") int localConnectionLimitMax;
	@Value("${cassandraDriver.connections.remote.min:-1}") int remoteConnectionLimitMin;
	@Value("${cassandraDriver.connections.remote.min:-1}") int remoteConnectionLimitMax;
	@Value("${cassandraDriver.speculativeExecution.delay:-1}") int speculativeExecutionDelay;
	@Value("${cassandraDriver.speculativeExecution.executions:-1}") int speculativeExecutionExecutions;
	@Value("${cassandraDriver.timeout.read:-1}") int timeoutRead;
	@Value("${cassandraDriver.timeout.connection:-1}") int timeoutConnection;
	@Value("${cassandraDriver.reconnection.delay:-1}") int reconnectionDelay;
	@Value("${cassandraDriver.reconnection.maxDelay:-1}") int reconnectionMaxDelay;
	@Value("${cassandraDriver.features.driverConfig:#{null}}") String featuresDriverConfig;
	@Value("${cassandraDriver.features.solr:#{null}}") String featuresSolr;
	@Value("${cassandraDriver.ssl.node:#{null}}") String sslNode;
	@Value("${cassandraDriver.ssl.truststore.path:#{null}}") String sslTruststorePath;
	@Value("${cassandraDriver.ssl.truststore.password:#{null}}") String sslTruststorePassword;

    /**
     * Spring bean to auto configure CassandraDriver
     * @return CassandraDriver
     */
	@Primary
    @Bean
    public CassandraDriver cassandraDriver() {

    	CassandraDriver.Builder cassandraDriverBuilder = CassandraDriver.builder();

    	if(contactPoints!=null) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withContactPoints(contactPoints);
    	}

    	if(keyspace!=null) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withKeyspace(keyspace);
    	}

    	if(localDC!=null) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withLocalDC(localDC);
    	}

    	if(port!=-1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withPort(port);
    	}

    	if(username != null) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withUsername(username);
    	}

    	if(password!=null) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withPassword(password);
    	}

    	if(defaultConsistencyLevel!=null) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withDefaultConsistencyLevel(defaultConsistencyLevel);
    	}

    	if(defaultClusterType!=null) {
	    	switch(defaultClusterType) {
	    		case "HA":
	    			cassandraDriverBuilder = cassandraDriverBuilder.withHACluster();
	    			break;
	    		case "Single":
	    			cassandraDriverBuilder = cassandraDriverBuilder.withSingleDCCluster();
	    			break;
	    	}
    	}

    	if(saveNulls!=null && saveNulls.equals("enabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withoutSaveNulls();
    	}

    	if(saveNulls!=null && saveNulls.equals("disabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withoutSaveNulls();
    	}

    	if(useRemoteConnections != -1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withUseRemoteConnections(useRemoteConnections);
    	}

    	if(localConnectionLimitMin!= -1 && localConnectionLimitMax != -1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withLocalConnectionLimit(localConnectionLimitMin, localConnectionLimitMax);
    	}

    	if(remoteConnectionLimitMin!= -1 && remoteConnectionLimitMax != -1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withRemoteConnectionLimit(remoteConnectionLimitMin, remoteConnectionLimitMax);
    	}

    	if(speculativeExecutionDelay!= -1 && speculativeExecutionExecutions != -1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withSpeculativeExecution(speculativeExecutionDelay,speculativeExecutionExecutions);
    	}

    	if(timeoutRead!= -1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withReadTimeout(timeoutRead);
    	}

    	if(timeoutConnection!= -1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withConnectionTimeout(timeoutConnection);
    	}

    	if(reconnectionDelay!= -1 && reconnectionMaxDelay != -1) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withReconnection(reconnectionDelay, reconnectionMaxDelay);
    	}

    	if(featuresDriverConfig!=null && featuresDriverConfig.equals("enabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withDriverConfig();
    	}

    	if(featuresDriverConfig!=null && featuresDriverConfig.equals("disabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withoutDriverConfig();
    	}

    	if(featuresSolr!=null && featuresSolr.equals("enabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withSolr();
    	}

    	if(featuresSolr!=null && featuresSolr.equals("disabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withoutSolr();
    	}

    	if(sslNode!=null && sslNode.equals("enabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withSSL();
    	}

    	if(sslNode!=null && sslNode.equals("disabled")) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withoutSSL();
    	}

    	if(sslTruststorePath!=null) {
    		if(sslTruststorePassword!=null) {
        		cassandraDriverBuilder = cassandraDriverBuilder.withTrustStore(sslTruststorePath, sslTruststorePassword);
    		}
    		else {
        		cassandraDriverBuilder = cassandraDriverBuilder.withTrustStore(sslTruststorePath, "");
    		}
    	}

    	if(defaultSolrDC!=null) {
    		cassandraDriverBuilder = cassandraDriverBuilder.withSolrDC(defaultSolrDC);
    	}


    	return cassandraDriverBuilder.build();
    }

    /**
     * Spring bean to auto configure CassandraAdminDriver
     * @return CassandraDriver
     */
    @Bean
    public CassandraAdminDriver cassandraAdminDriver() {
    	return new CassandraAdminDriver(cassandraDriver());
    }

}
