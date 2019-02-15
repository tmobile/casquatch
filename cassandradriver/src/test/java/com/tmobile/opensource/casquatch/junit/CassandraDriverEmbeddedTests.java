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

package com.tmobile.opensource.casquatch.junit;

import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.exceptions.DriverException;
import com.tmobile.opensource.casquatch.models.junittest.TableName;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CassandraDriverEmbeddedTests extends CassandraDriverTestSuite {

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException {
		EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE, EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT);
        //Create a system connection for creating keyspace
        db = CassandraDriver.builder()
        		.withContactPoints(EmbeddedCassandraServerHelper.getHost())
        		.withPort(EmbeddedCassandraServerHelper.getNativeTransportPort())
        		.withLocalDC("cassandraunit")
        		.withKeyspace("system")
        		.withoutDriverConfig()
        		.build();
        
        createSchema();
        db.close();
        db = new CassandraDriver("cassandra", "cassandra", EmbeddedCassandraServerHelper.getHost(), EmbeddedCassandraServerHelper.getNativeTransportPort(), "cassandraunit", "junittest");
    }
    
    @Test
    public void testConstructor() {
    	CassandraDriver db = new CassandraDriver("cassandra", "cassandra", EmbeddedCassandraServerHelper.getHost(), EmbeddedCassandraServerHelper.getNativeTransportPort(), "cassandraunit", "junittest");
    }
    
    @Before
    public void beforeReconnect() {
        TableName obj = new TableName(20,21);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.save(TableName.class, obj);
    }
    
    @Test
    public void testReconnect() {
    	 CassandraDriver tmpdb = new CassandraDriver("cassandra", "cassandra", EmbeddedCassandraServerHelper.getHost(), EmbeddedCassandraServerHelper.getNativeTransportPort(), "cassandraunit", "junittest");
    	 tmpdb.close();
    	 TableName obj = tmpdb.getById(TableName.class, new TableName(20,21));

         assertEquals(obj.getColOne(),"ColumnOne");
         assertEquals(obj.getColTwo(),"ColumnTwo");
    }
}
