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
import com.tmobile.opensource.casquatch.models.junittest.JunitUdt;
import com.tmobile.opensource.casquatch.models.junittest.JunitUdtTable;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class CassandraDriverUDTTests {

    private static CassandraDriver db;
    private final static Logger logger = LoggerFactory.getLogger(CassandraDriverUDTTests.class);

    @BeforeClass
    public static void setUp() throws IOException, TTransportException {

        EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE, EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT);
        
        db = new CassandraDriver.Builder()
        		.withContactPoints(EmbeddedCassandraServerHelper.getHost())
        		.withPort(EmbeddedCassandraServerHelper.getNativeTransportPort())
        		.withLocalDC("cassandraunit")
        		.withKeyspace("system")
        		.withoutDriverConfig()
        		.build();
        
        try {
            db.execute("drop keyspace junitTest");
        } catch (DriverException e) {

        }

        db.execute("CREATE KEYSPACE junitTest WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1}  AND durable_writes = true");
        db.execute("CREATE TYPE junitTest.junit_udt (val1 text, val2 int)");
        db.execute("CREATE TABLE junitTest.junit_udt_table (id uuid primary key, udt frozen<junit_udt>)");

        //reconnect now that the tables and keyspace are created
        db.close();
        
        db = new CassandraDriver.Builder()
        		.withContactPoints(EmbeddedCassandraServerHelper.getHost())
        		.withPort(EmbeddedCassandraServerHelper.getNativeTransportPort())
        		.withLocalDC("cassandraunit")
        		.withKeyspace("junittest")
        		.withoutDriverConfig()
        		.build();
    }
    
    private JunitUdtTable generate() {    	
    	JunitUdtTable obj = new JunitUdtTable(UUID.randomUUID());
    	JunitUdt udt = new JunitUdt();
    	udt.setVal1(UUID.randomUUID().toString());
    	udt.setVal2(new Random().nextInt(100)+1);
    	obj.setUdt(udt);
    	return obj;
    }

    @Test
    public void testSave() {    
    	//Create and save
    	JunitUdtTable testObj = generate();
        db.save(JunitUdtTable.class, testObj);
        
        //Validate
        JunitUdtTable valObj = db.getById(JunitUdtTable.class,new JunitUdtTable(testObj.getId()));
        assertEquals(valObj.getUdt().getVal1(),testObj.getUdt().getVal1());
        assertEquals(valObj.getUdt().getVal2(),testObj.getUdt().getVal2());        
    }
    
    @Test
    public void testDelete() {
    	//Create and save
    	JunitUdtTable testObj = generate();
        db.save(JunitUdtTable.class, testObj);     
        
        //Delete        
        db.delete(JunitUdtTable.class, new JunitUdtTable(testObj.getId()));

        //Validate
        assertFalse(db.existsById(JunitUdtTable.class, new JunitUdtTable(testObj.getId())));
    }

    @AfterClass
    public static void shutdown() {
        db.close();
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }
}
