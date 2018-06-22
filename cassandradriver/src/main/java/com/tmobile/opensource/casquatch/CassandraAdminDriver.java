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

import org.springframework.beans.factory.annotation.Value;

import com.datastax.driver.core.Session;
import com.tmobile.opensource.casquatch.exceptions.DriverException;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;

/**
 * This object provides an advanced interface for connecting to Cassandra clusters within SDE which allows for functionality that through the abstraction and thus requires DSE imports. 
 *
 * @version 1.1
 */
public class CassandraAdminDriver extends CassandraDriver {
	
	/**
     * Initializes the Admin Driver
     * @param username Name of User
     * @param password Password of user
     * @param contactPoints Comma separated list of contact points for Cassandra cluster. Order is ignored.
     * @param port Port Cassandra is listening on. Typically 9042
     * @param localDC Which dc to consider local
     * @param keyspace Default keyspace
     */
    public CassandraAdminDriver(@Value("${cassandraDriver.username:cassandra}") String username, @Value("${cassandraDriver.password:cassandra}") String password, @Value("${cassandraDriver.contactPoints:localhost}") String contactPoints, @Value("${cassandraDriver.port:9042}") int port, @Value("${cassandraDriver.localDC}") String localDC, @Value("${cassandraDriver.keyspace}") String keyspace) {
		super(username, password, contactPoints, port, localDC, keyspace);
	}
    
	/**
     * Initializes the Admin Driver from driver reference
     * @param cassandraDriver driver reference
     */
    
    public CassandraAdminDriver(CassandraDriver cassandraDriver) {
    	super(cassandraDriver.config);
    }
    
    /**
     * Returns a raw Datastax session using the specified connection key
     * @param key specify the session key
     * @return Session object using default connection
     * @throws DriverException - Driver exception mapped to error code
     */
    public Session getDatastaxSession(String key) throws DriverException {
    	return this.getSession(key);
    }

    /**
     * Returns a raw Datastax session using the appropriate connection for the supplied class
     * @param <T> Domain Object for results
     * @param c Class of object
     * @return Session object using default connection
     * @throws DriverException - Driver exception mapped to error code
     */
    public <T extends AbstractCassandraTable> Session getDatastaxSession(Class<T> c) throws DriverException {
    	return this.getDatastaxSession(getConnectionKey(c));
    }

    /**
     * Returns a raw Datastax session using the default connection
     * @return Session object using default connection
     * @throws DriverException - Driver exception mapped to error code
     */
    public Session getDatastaxSession() throws DriverException {
    	return this.getDatastaxSession("default");
    }

}
