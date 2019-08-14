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

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.policies.ConstantSpeculativeExecutionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy;
import com.datastax.driver.core.policies.FallthroughRetryPolicy;
import com.datastax.driver.core.policies.HostFilterPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import com.datastax.driver.core.policies.SpeculativeExecutionPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.tmobile.opensource.casquatch.exceptions.DriverException;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;
import com.tmobile.opensource.casquatch.models.shared.DriverConfig;

/**
 * This object provides a standard interface for connecting to Cassandra clusters within SDE.
 *
 * @version 1.0
 * @since   2018-02-26
 */
public class CassandraDriver {

	public static class Builder {
		protected class Configuration {		
			class SpeculativeExecution {
				int delay;
				int executions;
			}	
			class Defaults {
				String clusterType;
				String consistencyLevel;		
				String solrDC;
				boolean saveNulls;
			}
			class Features {
				boolean driverConfig;
				boolean solr;
			}
			class Connections {
				class Limit {
					int min;
					int max;
				}
				Limit local = new Limit();
				Limit remote = new Limit();
			}	
			class Timeout {
				int read;
				int connection;
			}	
			class Reconnection {
				int delay;
				int maxDelay;			
			}	
			class SSL {
				class Keystore {
					String path;
					String password;
				}
				boolean node;
				Keystore truststore = new Keystore();
			}			
			class LoadBalancing {				
				class Token {
					boolean enabled;
				}
				class Filter {
					class Workload {
						List<String> workloads = new ArrayList<String>();
						boolean enabled;
					}
					class DC {
						List<String> datacenters = new ArrayList<String>();
						boolean enabled;
					}
					Workload workload = new Workload();
					DC dc = new DC();
				}
				Token token = new Token();
				Filter filter = new Filter();
			}
			
			Connections connections = new Connections();
			Timeout timeout = new Timeout();
			Reconnection reconnection = new Reconnection();
			SpeculativeExecution speculativeExecution = new SpeculativeExecution();
			Defaults defaults = new Defaults();
			Features features = new Features();
			SSL ssl = new SSL();
			LoadBalancing loadBalancing = new LoadBalancing();
			
			String username;
			String password;						
			String localDC;			
			String keyspace;			
			int port;			
			List<String> contactPoints = new ArrayList<String>();
			int useRemoteConnections;
			
			public Configuration() {
				this.contactPoints.add("localhost");
				this.port=9042;
				this.username = "cassandra";
				this.password = "cassandra";			
				this.defaults.consistencyLevel = "LOCAL_QUORUM";
				this.defaults.clusterType = "HA";
				this.useRemoteConnections= 2;			
				this.connections.local.min=1;
				this.connections.local.max=3;
				this.connections.remote.min=1;
				this.connections.remote.max=1;
				this.speculativeExecution.delay=500;
				this.speculativeExecution.executions=2;
				this.timeout.read=500;
				this.timeout.connection=12000;
				this.reconnection.delay=500;
				this.reconnection.maxDelay=300000;
				this.features.driverConfig=true;
				this.features.solr=true;
				this.defaults.solrDC="search";
				this.defaults.saveNulls=false;
				this.ssl.node=false;
				this.loadBalancing.filter.dc.enabled=false;
				this.loadBalancing.filter.workload.enabled=false;
				this.loadBalancing.token.enabled=true;
			}
			
			public String toString() {
				try {
					ObjectMapper mapper = new ObjectMapper();
					mapper.setVisibility(PropertyAccessor.FIELD,Visibility.ANY);
					return mapper.writeValueAsString(this).replace(this.password, "MASKED");
				} catch (JsonProcessingException e) {
					logger.debug("Failed to serialize config",e);
					return "Failed to serialize";
				}
			}
			
			public boolean validate() throws DriverException {
		    	logger.debug("Configuration Validation: "+this.toString());
				
		    	if(this.contactPoints == null || this.contactPoints.isEmpty())
		    		throw new DriverException(401,"Contact Points are required");
		    		
		    	if(this.keyspace == null || this.keyspace.isEmpty())
		    		throw new DriverException(401,"Keyspace is required");		    	
		    	
		    	return true;
				
			}
		}
		
		private Builder.Configuration config;
		
	    /**
	     * CassandraDriver Builder constructor. Configures default settings.
	     */
		public Builder() {
			config = new Builder.Configuration();	
			
		}
		
	    /**
	     * Builder with tuned defaults
	     * @return Reference to Builder object
	     */	
		public static Builder withDefaults() {
			return new Builder();
		}
		
		/**
	     * Builder with Cassandra workload tuned defaults
	     * @return Reference to Builder object
	     */		
		public static Builder withCassandraDefaults() {
			return Builder.withDefaults()
					.withWorkload("Cassandra")
					.withoutSolr();
		}
		
	    /**
	     * Builder with Solr workload tuned defaults
	     * @return Reference to Builder object
	     */		
		public static Builder withSolrDefaults() {
			return Builder.withDefaults()
					.withWorkload("Search")
					.withoutSolrDC()
					.withReadTimeout(12000)
					.withDefaultConsistencyLevel("LOCAL_ONE");
		}
		
	    /**
	     * Build with username
	     * @param username connection username
	     * @return Reference to Builder object
	     */
		public Builder withUsername(String username) {
			config.username = username;
			return this;
		}
		
	    /**
	     * Build with password
	     * @param password connection password
	     * @return Reference to Builder object
	     */
		public Builder withPassword(String password) {
			config.password = password;
			return this;
		}
		
	    /**
	     * Build with local datacenter
	     * @param localDC local data center
	     * @return Reference to Builder object
	     */
		public Builder withLocalDC(String localDC) {
			config.localDC = localDC;
			return this;
		}	
		
	    /**
	     * Build with port
	     * @param port connection port
	     * @return Reference to Builder object
	     */
		public Builder withPort(int port) {
			config.port=port;
			return this;
		}	
		
	    /**
	     * Build with keyspace
	     * @param keyspace connection keyspace
	     * @return Reference to Builder object
	     */
		public Builder withKeyspace(String keyspace) {
			config.keyspace=keyspace;
			return this;
		}	
		
	    /**
	     * Build with comma separated list of contact points
	     * @param contactPoints connection contact points separated by comma
	     * @return Reference to Builder object
	     */
		public Builder withContactPoints(String contactPoints) {
			config.contactPoints = Arrays.asList(contactPoints.split(","));
			return this;
		}
		
	    /**
	     * Build with list of contact points
	     * @param contactPoints connection contact points
	     * @return Reference to Builder object
	     */
		public Builder withContactPoints(List<String> contactPoints) {
			config.contactPoints = contactPoints;
			return this;
		}
		
	    /**
	     * Build with default consistency level
	     * @param defaultConsistencyLevel default consistency level
	     * @return Reference to Builder object
	     */
		public Builder withDefaultConsistencyLevel(String defaultConsistencyLevel) {
			config.defaults.consistencyLevel = defaultConsistencyLevel;
			return this;
		}
		
	    /**
	     * Build with configured remote connections per query
	     * @param remoteConnections remote connection count
	     * @return Reference to Builder object
	     */
		public Builder withUseRemoteConnections(int remoteConnections) {
			config.useRemoteConnections= remoteConnections;
			return this;
		}
		
	    /**
	     * Build with local connection limits
	     * @param min min connections
	     * @param max max connections
	     * @return Reference to Builder object
	     */
		public Builder withLocalConnectionLimit(int min, int max) {
			config.connections.local.min = min;
			config.connections.local.max = max;
			return this;
		}
		
	    /**
	     * Build with remote connection limits
	     * @param min min connections
	     * @param max max connections
	     * @return Reference to Builder object
	     */
		public Builder withRemoteConnectionLimit(int min, int max) {
			config.connections.remote.min = min;
			config.connections.remote.max = max;
			return this;
		}
		
	    /**
	     * Build with speculative execution.
	     * @param delay number of ms to wait before triggering
	     * @param executions max number of executions
	     * @return Reference to Builder object
	     */
		public Builder withSpeculativeExecution(int delay, int executions) {
			config.speculativeExecution.delay = delay;
			config.speculativeExecution.executions = executions;
			return this;
		}
		
	    /**
	     * Build with reconnection
	     * @param delay number of ms to wait before reconnecting
	     * @param maxDelay maximum number of ms to wait
	     * @return Reference to Builder object
	     */
		public Builder withReconnection(int delay, int maxDelay) {
			config.reconnection.delay = delay;
			config.reconnection.maxDelay = maxDelay;
			return this;
		}
		
	    /**
	     * Build with read timeout
	     * @param readTimeout timeout in ms
	     * @return Reference to Builder object
	     */
		public Builder withReadTimeout(int readTimeout) {
			config.timeout.read = readTimeout;
			return this;
		}
		
	    /**
	     * Build with connection timeout
	     * @param connectionTimeout timeout in ms
	     * @return Reference to Builder object
	     */
		public Builder withConnectionTimeout(int connectionTimeout) {
			config.timeout.connection = connectionTimeout;
			return this;
		}
		
	    /**
	     * Build with a single data center
	     * @return Reference to Builder object
	     */
		public Builder withSingleDCCluster() {
			config.defaults.clusterType = "Single" ;
			return this;
		}
		
	    /**
	     * Build with a highly available cluster (multiple data centers)
	     * @return Reference to Builder object
	     */
		public Builder withHACluster() {
			config.defaults.clusterType = "HA" ;
			return this;
		}
		
	    /**
	     * Build with driver configuration table
	     * @return Reference to Builder object
	     */
		public Builder withDriverConfig() {
			config.features.driverConfig=true;
			return this;
		}
		
	    /**
	     * Build without driver configuration table
	     * @return Reference to Builder object
	     */
		public Builder withoutDriverConfig() {
			config.features.driverConfig=false;
			return this;
		}
		
	    /**
	     * Build with solr enabled
	     * @return Reference to Builder object
	     */
		public Builder withSolr() {
			config.features.solr=true;
			return this;
		}
		
	    /**
	     * Build without solr enabled
	     * @return Reference to Builder object
	     */
		public Builder withoutSolr() {
			config.features.solr=false;
			return this;
		}
		
	    /**
	     * Build with solr data center
	     * @param dc datacenter for solr
	     * @return Reference to Builder object
	     */
		public Builder withSolrDC(String dc) {
			config.defaults.solrDC=dc;
			return this;
		}
		
	    /**
	     * Build with solr data center set to null
	     * @return Reference to Builder object
	     */
		public Builder withoutSolrDC() {
			config.defaults.solrDC=null;
			return this;
		}
		
	    /**
	     * Build without saving nulls
	     * @return Reference to Builder object
	     */
		public Builder withSaveNulls() {
			config.defaults.saveNulls=true;
			return this;
		}
		
	    /**
	     * Build with saving nulls
	     * @return Reference to Builder object
	     */
		public Builder withoutSaveNulls() {
			config.defaults.saveNulls=false;
			return this;
		}
		
	    /**
	     * Build with ssl
	     * @return Reference to Builder object
	     */
		public Builder withSSL() {
			config.ssl.node=true;
			return this;
		}
		
	    /**
	     * Build without ssl
	     * @return Reference to Builder object
	     */
		public Builder withoutSSL() {
			config.ssl.node=false;
			return this;
		}
		
	    /**
	     * BBuild with defined truststore
	     * @param path path to truststore
	     * @param password truststore password
	     * @return Reference to Builder object
	     */
		public Builder withTrustStore(String path, String password) {
			config.ssl.truststore.path=path;
			config.ssl.truststore.password=password;
			return this;
		}
		
	    /**
	     * Build with token aware policy
	     * @return Reference to Builder object
	     */
		public Builder withTokenAware() {
			config.loadBalancing.token.enabled=true;
			return this;
		}
		
	    /**
	     * Build without token aware policy
	     * @return Reference to Builder object
	     */
		public Builder withoutTokenAware() {
			config.loadBalancing.token.enabled=false;
			return this;
		}
		
		/**
	     * Build without workload filtering
	     * @return Reference to Builder object
	     */
		public Builder withoutWorkloadFilter() {
			config.loadBalancing.filter.workload.enabled=false;
			config.loadBalancing.filter.workload.workloads.clear();
			return this;
		}
		
	    /**
	     * Build with workload filtering and allow the defined workload. If workload is Search then solrDC is cleared.
	     * @param workload add workload list
	     * @return Reference to Builder object
	     */
		public Builder withWorkload(String workload) {
			config.loadBalancing.filter.workload.enabled=true;
			config.loadBalancing.filter.workload.workloads.add(workload);
			if(workload.equals("Search")) {
				return this.withoutSolrDC();
			}
			else {
				return this;				
			}
		}
		
	    /**
	     * Build with workload filtering and use the list of workloads
	     * @param workloads list of workloads
	     * @return Reference to Builder object
	     */
		public Builder withWorkloads(List<String> workloads) {
			config.loadBalancing.filter.workload.enabled=true;
			config.loadBalancing.filter.workload.workloads = workloads;
			return this;
		}
		
	    /**
	     * Build with workload filtering and use the list of workloads
	     * @param workloads list of workloads separate by comma
	     * @return Reference to Builder object
	     */
		public Builder withWorkloads(String workloads) {
			config.loadBalancing.filter.workload.enabled=true;
			config.loadBalancing.filter.workload.workloads = Arrays.asList(workloads.split(","));
			return this;
		}
		
	    /**
	     * Build without data center filtering
	     * @return Reference to Builder object
	     */
		public Builder withoutDataCenterFilter() {
			config.loadBalancing.filter.dc.enabled=false;
			config.loadBalancing.filter.dc.datacenters.clear();
			return this;
		}
		
	    /**
	     * Build with data center filtering and allow the provided datacenter
	     * @param datacenter add datacenter to list
	     * @return Reference to Builder object
	     */
		public Builder withDataCenter(String datacenter) {
			config.loadBalancing.filter.dc.enabled=true;
			config.loadBalancing.filter.dc.datacenters.add(datacenter);
			return this;
		}
		
	    /**
	     * Build with data center filtering and allow the provided datacenters
	     * @param datacenters list of datacenters
	     * @return Reference to Builder object
	     */
		public Builder withDataCenters(List<String> datacenters) {
			config.loadBalancing.filter.dc.enabled=true;
			config.loadBalancing.filter.dc.datacenters = datacenters;
			return this;
		}
		
	    /**
	     * Build with data center filtering and allow the provided datacenters
	     * @param datacenters list of datacenters separated by comma
	     * @return Reference to Builder object
	     */
		public Builder withDataCenters(String datacenters) {
			config.loadBalancing.filter.dc.enabled=true;
			config.loadBalancing.filter.dc.datacenters = Arrays.asList(datacenters.split(","));
			return this;
		}

		
	    /**
	     * Get configuration object
	     * @return config Configuration instance
	     */	
		private Builder.Configuration getConfiguration() {
			return config;
		}
		
	    /**
	     * Build the defined CassandraDriver
	     * @return CassandraDriver Configured driver object
	     */
		public CassandraDriver build() {
			return CassandraDriver.buildFrom(getConfiguration());
		}
		
	    /**
	     * Build the defined CassandraDriver
	     * @param driver Reference to a configured driver object
	     * @return CassandraDriver Configured driver object
	     */
		public Builder clone(CassandraDriver driver) {
			this.config = driver.config;
			return this;
		}
	}

    private Map<String,Cluster> clusterMap;
    private Map<String,Session> sessionMap;
    private Map<String,MappingManager> mappingManagerMap;
    private DatabaseCache<DriverConfig> driverConfig;

    protected Builder.Configuration config;

    private final static Logger logger = LoggerFactory.getLogger(CassandraDriver.class);

    /**
     * Validates a Builder configuration and returns the configured driver. Tied to .build() procedure
     * @param config driver configuration
     */
    private static CassandraDriver buildFrom(Builder.Configuration config) throws DriverException{
    	if(config == null)
    		throw new DriverException(401,"Configuration is required");
    	
    	return new CassandraDriver(config);    	
    }
    
    /**
     * Cassandra Driver Builder. Please refer to builder docs for details
     * @return builder Instance of CassandraDriver.Builder
     */
    
    public static CassandraDriver.Builder builder() {
    	logger.debug("Using builder");
    	return new CassandraDriver.Builder();
    }
    
    /**
     * Initializes the Driver with configuration object
     * @param config driver configuration
     */
    protected CassandraDriver(Builder.Configuration config) {
    	logger.info("Using Version: {}",CassandraDriver.getVersion());
    	config.validate();
    	this.config = config;
        this.clusterMap = new HashMap<String, Cluster>();
        this.sessionMap = new HashMap<String, Session>();
        this.mappingManagerMap = new HashMap<String, MappingManager>();
        this.driverConfig = new DatabaseCache<DriverConfig>(DriverConfig.class, this);
    }

    /**
     * Initializes the Driver
     * @param username Name of User
     * @param password Password of user
     * @param contactPoints Comma separated list of contact points for Cassandra cluster. Order is ignored.
     * @param port Port Cassandra is listening on. Typically 9042
     * @param localDC Which dc to consider local
     * @param keyspace Default keyspace
     */
    public CassandraDriver(String username, String password, String contactPoints, int port,String localDC, String keyspace) {
    	this(
			new Builder()
				.withUsername(username)
				.withPassword(password)
				.withContactPoints(contactPoints)
				.withPort(port)
				.withLocalDC(localDC)
				.withKeyspace(keyspace)
				.getConfiguration()
			);
    }

    /**
     * Returns the CassandraDriver version information-
     * @return Cluster object for key
     */
    public static String getVersion() {
    	InputStream resourceAsStream = CassandraDriver.class.getResourceAsStream("/maven.properties");
    	Properties properties = new Properties();
    	try {
    		properties.load(resourceAsStream);
    		return "Casquatch "+(String) properties.get("version")+". Java Driver "+Cluster.getDriverVersion();
    	}
    	catch (Exception e) {
    		throw new DriverException(e);
    	}

    }

	/**
     * Get a cluster connection or create if missing. If key is default then the default cluster is used. If key is not then it makes a local only cluster connection to a DC of the given key
     * @param key Key for the cluster connection
     * @return Cluster object for key
     */
    private Cluster getCluster(String key) {
        Cluster cluster;
        if(! clusterMap.containsKey(key)) {
            switch(key) {
                case "default":
                	switch(this.config.defaults.clusterType) {
                		case "HA":
                			cluster = createHACluster(config.localDC);
                			break;
                		case "Single":
                		default:
            				cluster = createSingleDCCluster(config.localDC);
            				break;
                	}
                    break;
                default:
                    cluster = createSingleDCCluster(key);
            }
            logger.info("Created new cluster connection for key {}",key);
            clusterMap.put(key,cluster);
        }
        return clusterMap.get(key);
    }

    /**
     * Get a session for the key
     * @param key Key for the session
     * @return Session object for key
     * @throws DriverException - Driver exception mapped to error code
     */
    protected Session getSession(String key) throws DriverException {
        if (!sessionMap.containsKey(key)) {
            try {
                sessionMap.put(key, getCluster(key).connect(config.keyspace));
                logger.info("Opened new session in {} to {}",key,config.keyspace);
            }
            catch (Exception e) {
                DriverException driverException = new DriverException(e);
                throw driverException;
            }
        }
        return sessionMap.get(key);
    }

    /**
     * Gets the solr session
     * @return Session object for solr
     * @throws DriverException - Driver exception mapped to error code
     */
	protected Session getSolrSession() throws DriverException {
		if(!config.features.solr) {
			throw new DriverException(401,"Solr is disabled");
		}
		else if(config.loadBalancing.filter.workload.workloads.contains("Search") && config.defaults.solrDC.isEmpty()) {
			logger.debug("Using default cluster as workload was defined as Solr");
			return this.getSession("default");
		}
		else {
			logger.debug("Using {}",config.defaults.solrDC);
			return this.getSession(config.defaults.solrDC);
		}
	}

    /**
     * Get a mapping manager for the key
     * @param key Key for the Mapping Manager
     * @return Mapping Manager object for key
     */
    private MappingManager getMappingManager(String key) {
        if(!mappingManagerMap.containsKey(key)) {
            mappingManagerMap.put(key, new MappingManager(getSession(key)));
        }
        return mappingManagerMap.get(key);
    }

    /**
     * Initializes the Cluster that prefers local dc but allows remote dc
     * @param localDC Which dc to consider local
     * @return Cluster object for supplied details
     */
    private Cluster createHACluster(String localDC) {
        logger.info("Creating new HA cluster with local set to {}",localDC);

        DCAwareRoundRobinPolicy.Builder dcAwareRoundRobinPolicyBuilder = DCAwareRoundRobinPolicy.builder();

        if(localDC!=null) {
        	dcAwareRoundRobinPolicyBuilder = dcAwareRoundRobinPolicyBuilder.withLocalDc(localDC);
        }

        if(config.useRemoteConnections > 0) {
        	dcAwareRoundRobinPolicyBuilder =
    			dcAwareRoundRobinPolicyBuilder
    				.withUsedHostsPerRemoteDc(config.useRemoteConnections)
    				.allowRemoteDCsForLocalConsistencyLevel();
        }

        LoadBalancingPolicy loadBalancingPolicy = dcAwareRoundRobinPolicyBuilder.build();

        if(config.loadBalancing.token.enabled) {
        	loadBalancingPolicy = new TokenAwarePolicy(loadBalancingPolicy);
        }

        if(config.loadBalancing.filter.dc.enabled) {
        	loadBalancingPolicy = HostFilterPolicy.fromDCWhiteList(loadBalancingPolicy,config.loadBalancing.filter.dc.datacenters);
        }

        if(config.loadBalancing.filter.workload.enabled) {
        	try {
				Class<?> workloadFilterClass = Class.forName("com.tmobile.opensource.casquatch.policies.WorkloadFilterPolicy");
				Class<?>[] formalparameters = { LoadBalancingPolicy.class, List.class };
				Object[] effectiveParameters = new Object[] { loadBalancingPolicy, config.loadBalancing.filter.workload.workloads };
				loadBalancingPolicy = (LoadBalancingPolicy) workloadFilterClass.getMethod("fromWorkloadList", formalparameters ).invoke(null, effectiveParameters);
			} catch (Exception e) {
				throw new DriverException(402,"Workload filter requires Casquatch-EE");
			}

        	//loadBalancingPolicy = WorkloadFilterPolicy.fromWorkloadList(loadBalancingPolicy,  config.loadBalancing.filter.workload.workloads);
        }

        return createCluster(loadBalancingPolicy);
    }

    /**
     * Initializes the Cluster for a single datacenter that does not allow a remote dc
     * @param datacenter Which dc to use
     * @return Cluster object for supplied details
     */
    private Cluster createSingleDCCluster(String datacenter) {
        logger.info("Creating new Single DC cluster with datacenter set to {}",datacenter);

        LoadBalancingPolicy loadBalancingPolicy = DCAwareRoundRobinPolicy.builder()
                .withLocalDc(datacenter)
                .build();

        if(config.loadBalancing.token.enabled) {
        	loadBalancingPolicy = new TokenAwarePolicy(loadBalancingPolicy);
        }
        return createCluster(loadBalancingPolicy);
    }

    /**
     * Initializes the Cluster
     * @param loadBalancingPolicy Configured Load Balancing Policy
     * @return Cluster object for supplied details
     */
    private Cluster createCluster(LoadBalancingPolicy loadBalancingPolicy) throws DriverException {
        //Set the local DC to use min 1 connection (34k threads) up to 3 max
        PoolingOptions poolingOptions = new PoolingOptions()
                //.setNewConnectionThreshold(HostDistance.LOCAL, 200) //default
                //.setMaxRequestsPerConnection(HostDistance.LOCAL, 256) //default
                //.setIdleTimeoutSeconds(120) //default
                //.setNewConnectionThreshold(HostDistance.LOCAL, 800) //default
                //.setMaxRequestsPerConnection(HostDistance.LOCAL, 1024) //default
                .setConnectionsPerHost(HostDistance.LOCAL, config.connections.local.min, config.connections.local.max)
                .setConnectionsPerHost(HostDistance.REMOTE, config.connections.remote.min,config.connections.remote.max);


        SpeculativeExecutionPolicy speculativeExecutionPolicy = new ConstantSpeculativeExecutionPolicy(
                config.speculativeExecution.delay,
                config.speculativeExecution.executions
        );

        SocketOptions socketOptions = new SocketOptions()
                .setConnectTimeoutMillis(config.timeout.connection)
                .setReadTimeoutMillis(config.timeout.read);

        ExponentialReconnectionPolicy reconnectionPolicy = new ExponentialReconnectionPolicy(config.reconnection.delay, config.reconnection.maxDelay);

        RetryPolicy retryPolicy = FallthroughRetryPolicy.INSTANCE;

        Cluster.Builder clusterBuilder = Cluster.builder()
                .addContactPoints(config.contactPoints.toArray(new String[0]))
                .withLoadBalancingPolicy(loadBalancingPolicy)
                .withPort(config.port)
                .withSpeculativeExecutionPolicy(speculativeExecutionPolicy)
                .withCredentials(config.username, config.password)
                .withPoolingOptions(poolingOptions)
                .withSocketOptions(socketOptions)
                .withReconnectionPolicy(reconnectionPolicy)
                .withRetryPolicy(retryPolicy);

        if (config.ssl.node) {
        	try {
        		if (config.ssl.truststore.path!= null && !config.ssl.truststore.path.isEmpty()) {
					KeyStore keyStore = KeyStore.getInstance("JKS");
					InputStream trustStore = new FileInputStream(config.ssl.truststore.path);
					keyStore.load(trustStore, config.ssl.truststore.password.toCharArray());
					TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustManagerFactory.init(keyStore);
					trustStore.close();

	                SSLContext sslContext = SSLContext.getInstance("TLS");
	                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

	                RemoteEndpointAwareJdkSSLOptions sslOptions = new RemoteEndpointAwareJdkSSLOptions(sslContext, null) {};

		        	clusterBuilder = clusterBuilder.withSSL(sslOptions);
        		}
        		else {
        			clusterBuilder  = clusterBuilder.withSSL();
        		}
        	}
            catch (Exception e) {
                DriverException driverException = new DriverException(e);
                throw driverException;
            }
        }

        Cluster cluster = clusterBuilder.build();

        return cluster;
    }

    /**
     * Convenience function for raw CQL execution. Ignores results
     * This should on be used on edge cases and is not type safe. Most queries should go through mapped objects.
     * @param cql query
     * @throws DriverException - Driver exception mapped to error code
     */
    public void execute(String cql) throws DriverException{
        try {
            logger.debug("Executing {} on default",cql);
            this.getSession(getConnectionKey("default")).execute(cql);
        }
        catch (Exception e) {
            DriverException driverException = new DriverException(e);
            throw driverException;
        }
    }

    /**
     * Procedure executes query and return first row. It does not support consistency level settings of driver_config.
     * This should on be used on edge cases and is not type safe. Most queries should go through mapped objects.
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param cql query
     * @return ResultSet containing one row for query
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> T executeOne(Class<T> c, String cql) throws DriverException {
        logger.debug("Executing {} on {}",cql,getConnectionKey(c));
        try {
        	return this.getMapper(c).map(this.getSession(getConnectionKey(c)).execute(cql)).one();
	    }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Procedure executes query and returns all rows. It does not support consistency level settings of driver_config.
     * This should on be used on edge cases and is not type safe. Most queries should go through mapped objects.
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param cql cql query
     * @return List of objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> List<T> executeAll(Class<T> c, String cql) throws DriverException {
        logger.debug("Executing {} on {}",cql,getConnectionKey(c));
        try {
        	return this.getMapper(c).map(this.getSession(getConnectionKey(c)).execute(cql)).all();
        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Get an object by passing an instance of the given object with the key populated. All other fields are ignored
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing keys populated
     * @return Object
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> T getById(Class<T> c, T o) throws DriverException {
		logger.debug("Getting {}.{} values {} from {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));
		try {
            return this.getMapper(c).get(buildID(c,o,"read"));
        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Get one object from a partition by passing an instance of the given object with the partition key and optionally clustering keys populated. All other fields are ignored
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing keys populated
     * @return Instance of Objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> T getOneById(Class<T> c, T o) throws DriverException {
		logger.debug("Getting One {}.{} values {} from {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));

		try {
        	Select select = this.generateSelectQuery(c, o);
	       	logger.debug("Running Query: {}",select.getQueryString());
        	return this.getMapper(c).map(this.getSession(getConnectionKey(c)).execute(select)).one();

        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Get all objects from a partition by passing an instance of the given object with the partition key and optionally clustering keys populated. All other fields are ignored
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing keys populated
     * @return List of Objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> List<T> getAllById(Class<T> c, T o) throws DriverException {
        logger.debug("Getting All {}.{} values {} from {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));
        try {
        	Select select = this.generateSelectQuery(c, o);
	       	logger.debug("Running Query: {}",select.getQueryString());
        	return this.getMapper(c).map(this.getSession(getConnectionKey(c)).execute(select)).all();

        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Generate a select query using annotations and reflection
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing keys populated
     * @return Select object
     * @throws DriverException - Driver exception mapped to error code
     */
    protected <T extends AbstractCassandraTable> Select generateSelectQuery(Class<T> c, T o) throws DriverException {
        try {
        	Select select = QueryBuilder.select().from(c.getAnnotation(Table.class).name());
	       	for(Field val : c.getDeclaredFields()) {
	       		 if(val.getAnnotationsByType(com.datastax.driver.mapping.annotations.PartitionKey.class).length > 0) {
	       			if(o.getClass().getMethod("get"+StringUtils.capitalize(val.getName())).invoke(o) != null) {
	       				select.where().and(QueryBuilder.eq(val.getAnnotation(com.datastax.driver.mapping.annotations.Column.class).name(), o.getClass().getMethod("get"+StringUtils.capitalize(val.getName())).invoke(o)));
	       			}
	       		 }
	       		 if(val.getAnnotationsByType(com.datastax.driver.mapping.annotations.ClusteringColumn.class).length > 0) {
	       			if(o.getClass().getMethod("get"+StringUtils.capitalize(val.getName())).invoke(o) != null) {
	       				select.where().and(QueryBuilder.eq(val.getAnnotation(com.datastax.driver.mapping.annotations.Column.class).name(), o.getClass().getMethod("get"+StringUtils.capitalize(val.getName())).invoke(o)));
	       			}
	       		 }
	         }
        	return select;
        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Generate a solr query using annotations and reflection
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing values populated
     * @return Select object
     * @throws DriverException - Driver exception mapped to error code
     */
    protected <T extends AbstractCassandraTable> Select generateSolrQuery(Class<T> c, T o) throws DriverException {
        try {
        	Select select = QueryBuilder.select().from(c.getAnnotation(Table.class).name());
	       	for(Field val : c.getDeclaredFields()) {
	       		 if(val.getAnnotationsByType(com.datastax.driver.mapping.annotations.Column.class).length > 0) {
	       			if(o.getClass().getMethod("get"+StringUtils.capitalize(val.getName())).invoke(o) != null) {
	       				select.where().and(QueryBuilder.eq(val.getAnnotation(com.datastax.driver.mapping.annotations.Column.class).name(), o.getClass().getMethod("get"+StringUtils.capitalize(val.getName())).invoke(o)));
	       			}
	       		 }
	         }
        	return select;
        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Helper function for solr queries
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param selct prepared select statement
     * @return Object containing results
     * @throws DriverException - Driver exception mapped to error code
     */
     private <T extends AbstractCassandraTable> Result<T> executeSolr(Class<T> c, Select select) throws DriverException {
         try {
 	       	logger.debug("Running Query: {} against Solr",select.getQueryString());
         	return this.getMapper(c).map(this.getSolrSession().execute(select));

         }
         catch (InvalidQueryException e) {
         	DriverException driverException;
         	if(e.getMessage().contains("ALLOW FILTERING")) {
         		driverException = new DriverException(402,"Query requires DSE Search >= 6.0.2");
         	}
         	else {
         		driverException = new DriverException(e);
         	}
         	throw driverException;
         }
 	    catch (Exception e) {
 	        DriverException driverException = new DriverException(e);
 	        throw driverException;
 	    }
    }

    /**
     * Get all Objects from Solr by supplying a populated object. Defaults to a limit of 10
     * WARNING: This requires at least DSE Search 6.0
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return List of Objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> List<T> getAllBySolr(Class<T> c, T o) throws DriverException {
        return getAllBySolr(c,o,10);
    }

    /**
     * Get all Objects from Solr by supplying a populated object.
     * WARNING: This requires at least DSE Search 6.0
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param limit limit the number of results
     * @return List of Objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> List<T> getAllBySolr(Class<T> c, T o, int limit) throws DriverException {
        Select select = this.generateSolrQuery(c, o).limit(limit);
        return this.executeSolr(c, select).all();
    }

    /**
     * Get  a list of objects by supplying a solr query. Defaults to limit of 10
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @return List of Objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> List<T> getAllBySolr(Class<T> c, String solrQueryString) throws DriverException {
    	return getAllBySolr(c,solrQueryString,10);
    }

    /**
     *  Get a list of objects by supplying a solr query
     * @param <T> Domain Object for results
     * @param c Class of object     *
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @param limit limit the number of results
     * @return List of Objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> List<T> getAllBySolr(Class<T> c, String solrQueryString, int limit) throws DriverException {
    	Select select = QueryBuilder.select().from(c.getAnnotation(Table.class).name()).where().and(QueryBuilder.eq("solr_query", solrQueryString)).limit(limit);
        return this.executeSolr(c, select).all();
    }

    /**
     * Get a list of objects from solr by specifying a query
     * @param <T> Domain Object for results
     * @param c Class of object     *
     * @param cql Raw CQL statement
     * @return List of Objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> List<T> getAllBySolrCQL(Class<T> c, String cql) throws DriverException {
         try {
    		logger.debug("Running Query: {} against Solr",cql);
    		return this.getMapper(c).map(this.getSolrSession().execute(cql)).all();
         }
         catch (InvalidQueryException e) {
         	DriverException driverException;
         	if(e.getMessage().contains("ALLOW FILTERING")) {
         		driverException = new DriverException(402,"Query requires DSE Search >= 6.0.2");
         	}
         	else {
         		driverException = new DriverException(e);
         	}
         	throw driverException;
         }
 	    catch (Exception e) {
 	        DriverException driverException = new DriverException(e);
 	        throw driverException;
 	    }
    }

    /**
     * Get count for solr query
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @return count of results
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> Long getCountBySolr(Class<T> c, String solrQueryString) throws DriverException {
        logger.debug("Getting Count from {}.{} with solar_query {} from {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),solrQueryString,getConnectionKey("solar"));

		try {
            Select select = QueryBuilder.select().countAll().from(c.getAnnotation(Table.class).name());
            select.where().and(QueryBuilder.eq("solr_query", solrQueryString));
            logger.debug("Running Query: {}",select.getQueryString());
            ResultSet result = this.getSolrSession().execute(select);
            if(result != null) {
                List<Row> rowList = result.all();
                if(rowList != null && rowList.size()>0) {
                    return new Long(rowList.get(0).getLong(0));
                }
                else {
                    return new Long(0);
                }
            }
            else {
                return new Long(0);
            }
        }
         catch (Exception e) {
             DriverException driverException = new DriverException(e);
             throw driverException;
         }
    }

    /**
     * Build the ID string to pass to mapper function including any options
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Populated object
     * @param type read/write
     * @return Object array to pass to mapper function
     */
    private <T extends AbstractCassandraTable> Object[] buildID(Class<T> c, T o,String type)  {
        Object[] id = new Object[o.getID().length+1];
        System.arraycopy(o.getID(),0,id,0,o.getID().length);
        id[o.getID().length] = getConsistencyLevel(c,type);
        return id;
    }

    /**
     * Check if an  object exists by passing an instance of the given object with the key populated. All other fields are ignored
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing keys populated
     * @return True if exists, false if not
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> boolean existsById(Class<T> c, T o) throws DriverException {
        logger.debug("Checking for existing {}.{} values {} in {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));

		try {
        	T obj = this.getMapper(c).get(buildID(c,o,"read"));
            if (obj != null){
                return true;
            }
            else {
                return false;
            }
        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Delete an object by passing an instance of the given object with the key populated. All other fields are ignored
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing keys populated
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> void delete(Class<T> c, T o) throws DriverException {
		logger.debug("Deleting {}.{} with values {} from {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));

		try {
        	this.getMapper(c).delete(o,getConsistencyLevel(c,"write"));
        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Delete an object asynchronously by passing an instance of the given object with the key populated. All other fields are ignored
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Object containing keys populated
     * @return ListenableFuture for query
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> ListenableFuture<Void> deleteAsync(Class<T> c, T o) throws DriverException {
		logger.debug("Deleting asynchronously {}.{} with values {} from {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));

		try {
        	return this.getMapper(c).deleteAsync(o,getConsistencyLevel(c,"write"));
        }
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Save an object by passing an instance of the given object. Inserts or updates as necessary. NOTE: Nulls are not persisted to the database.
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Populated object
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> void save(Class<T> c, T o) throws DriverException{
		logger.debug("Saving to {}.{} with values {} to {}",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));

		try {
    		this.getMapper(c).save(o,getConsistencyLevel(c,"write"));
    	}
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Save an object asynchronously by passing an instance of the given object. Inserts or updates as necessary. All other fields are ignored
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param o Populated object
     * @return ListenableFuture for query
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> ListenableFuture<Void> saveAsync(Class<T> c, T o) throws DriverException{
		logger.debug("Saving (asynchronously) to {}.{} values {} to {} ",c.getAnnotation(Table.class).keyspace(),c.getAnnotation(Table.class).name(),o.toString(),getConnectionKey(c));

		try {
    		return this.getMapper(c).saveAsync(o,getConsistencyLevel(c,"write"));
    	}
	    catch (Exception e) {
	        DriverException driverException = new DriverException(e);
	        throw driverException;
	    }
    }

    /**
     * Get the connection key from the driver_config table. If not specified then uses "default"
     * @param <T> Domain Object for results
     * @param c Class of object
     * @return Connection Key.
     */
    protected <T extends AbstractCassandraTable> String getConnectionKey(Class<T> c) {
        String key = "default";
        if(c.isAnnotationPresent(Table.class)) {
            Table annotation = c.getAnnotation(Table.class);
            String tableName = annotation.name();
            key = getConnectionKey(tableName);
        }
        return key;
    }

    /**
     * Get mapper for the given class
     * @param <T> Domain Object for results
     * @param c Class of object
     * @return Mapper object for class
     */
    private <T extends AbstractCassandraTable> Mapper<T> getMapper(Class<T> c) {
    	Mapper<T> mapper = this.getMappingManager(getConnectionKey(c)).mapper(c);
    	if(config.defaults.saveNulls) {
    		mapper.setDefaultSaveOptions(Mapper.Option.saveNullFields(true));
    	}
    	else {
    		mapper.setDefaultSaveOptions(Mapper.Option.saveNullFields(false));
    	}
        return mapper;
    }

    /**
     * Get the connection key from the driver_config table. If not specified then uses "default"
     * @param tableName table name
     * @return Connection Key.
     */
    private String getConnectionKey(String tableName) {
        String key = "default";
        if (config.features.driverConfig && !tableName.equals("driver_config")) {
            DriverConfig tmpDriverConfig = null;
            driverConfig.get(tableName);
            if (tmpDriverConfig == null) {
            	tmpDriverConfig = driverConfig.get("default");
            }
            if (tmpDriverConfig != null) {
                key = tmpDriverConfig.getDataCenter();
                logger.info("Found Connection Key for {}",tableName);
            }
        }
        logger.debug("Connection Key set to {} for {}",key,tableName);
        return key;
    }

    /**
     * Get the consistency level from driver_config table for the query.
     * @param <T> Domain Object for results
     * @param c Class of object
     * @param type Read or Write
     * @return Mapper Option for consistency level
     */
    private <T extends AbstractCassandraTable> Mapper.Option getConsistencyLevel(Class<T> c,String type) {
        Mapper.Option option = Mapper.Option.consistencyLevel(ConsistencyLevel.valueOf(config.defaults.consistencyLevel));
        if(c.isAnnotationPresent(Table.class)) {
            Table annotation = c.getAnnotation(Table.class);
            String tableName = annotation.name();
            if(config.features.driverConfig && !tableName.equals("driver_config")) {
                DriverConfig tmpDriverConfig = null;
                tmpDriverConfig = driverConfig.get(tableName);
                if (config == null) {
                	tmpDriverConfig = driverConfig.get("default");
                }
                if (tmpDriverConfig != null) {
                    switch (type) {
                        case "read":
                            option = Mapper.Option.consistencyLevel(ConsistencyLevel.valueOf(tmpDriverConfig.getReadConsistency()));
                            break;
                        case "write":
                            option = Mapper.Option.consistencyLevel(ConsistencyLevel.valueOf(tmpDriverConfig.getWriteConsistency()));
                            break;
                    }
                }
            }
        }
        return option;
    }

    /**
     * Close cluster connections
     */
    @PreDestroy
    public void close() {
        for(String key : this.clusterMap.keySet()) {
            this.clusterMap.get(key).close();
            logger.info("Closed cluster connection for key {}",key);
        }

    }
}