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

public class CassandraDriverTests {

    private static CassandraDriver db;
    private final static Logger logger = LoggerFactory.getLogger(CassandraDriverTests.class);

    @BeforeClass
    public static void setUp() throws IOException, TTransportException {

        EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE, EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT);

        //Create a system connection for creating keyspace
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
        db.execute("CREATE TABLE junitTest.table_name (key_one int,key_two int,col_one text,col_two text,PRIMARY KEY ((key_one), key_two))");
        db.execute("CREATE TABLE junitTest.driver_config (\n" +
                "    table_name text PRIMARY KEY,\n" +
                "    data_center text,\n" +
                "    read_consistency text,\n" +
                "    write_consistency text,\n" +
                "    create_dttm timestamp,\n" +
                "    create_user text,\n" +
                "    mod_dttm timestamp,\n" +
                "    mod_user text\n" +
                ")");
        //db.execute("insert into junitTest.driver_config (table_name,data_center) values('default','cassandraunit')");
        //db.execute("insert into junitTest.driver_config (table_name,data_center) values('table_name','cassandraunit')");


        //reconnect now that the tables and keyspace are created
        db.close();
        db = new CassandraDriver("cassandra", "cassandra", EmbeddedCassandraServerHelper.getHost(), EmbeddedCassandraServerHelper.getNativeTransportPort(), "cassandraunit", "junittest");
    }

    @Test(expected = DriverException.class)
    public void testExecuteInvalidQueryException() {
        db.execute("CREATE TABLE junitTest.table_name (key_one int,key_two int,col_one text,col_two text)");
    }
    
    @Test(expected=DriverException.class)
    public void testBuilderNoParameters() {
    	CassandraDriver.builder().build();
    }
    
    @Test(expected=DriverException.class)
    public void testBuilderMissedParameters() {
    	CassandraDriver.builder()    
    			.withLocalDC("fail")
    			.build();
    }
    
    @Test
    public void testBuilderMinParameters() {
    	CassandraDriver.builder()    
    			.withLocalDC("fail")
    			.withKeyspace("fake")
    			.build();
    }
    
    @Test
    public void testConstructor() {
    	CassandraDriver db = new CassandraDriver("cassandra", "cassandra", EmbeddedCassandraServerHelper.getHost(), EmbeddedCassandraServerHelper.getNativeTransportPort(), "cassandraunit", "junittest");
    }

    @Test
    public void testSave() {
    	//Save object
        TableName obj = new TableName(1, 1);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.save(TableName.class, obj);
        
        //Validate
        TableName valObj = db.getById(TableName.class,new TableName(1,1));
        assertEquals(valObj.getColOne(),"ColumnOne");
        assertEquals(valObj.getColTwo(),"ColumnTwo");
        
    }

    @After
    public void afterSave() {
        TableName obj = new TableName(1, 2);
        db.delete(TableName.class, obj);
    }


    @Test
    public void testSaveAsync() {
        TableName obj = new TableName(3, 4);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.saveAsync(TableName.class, obj);
        
        //Validate
        TableName valObj = db.getById(TableName.class,new TableName(3,4));
        assertEquals(valObj.getColOne(),"ColumnOne");
        assertEquals(valObj.getColTwo(),"ColumnTwo");
    }

    @After
    public void afterSaveAsync() {
        TableName obj = new TableName(3, 4);
        db.delete(TableName.class, obj);
    }
    
    @Before
    public void beforeSelectById() {
        TableName obj = new TableName(5, 6);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.save(TableName.class, obj);
    }

    @Test
    public void testGetById() {
        TableName obj = new TableName(5, 6);
        obj = db.getById(TableName.class, obj);
        
        //Validate
        TableName valObj = db.getById(TableName.class,new TableName(5,6));
        assertEquals(valObj.getColOne(),"ColumnOne");
        assertEquals(valObj.getColTwo(),"ColumnTwo");
    }

    @After
    public void afterGetById() {
        TableName obj = new TableName(5, 6);
        db.delete(TableName.class, obj);
    }

    @Before
    public void beforeDelete() {
        TableName obj = new TableName(7, 8);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.save(TableName.class, obj);
    }

    @Test
    public void testDelete() {
        TableName obj = new TableName(7, 8);
        db.delete(TableName.class, obj);       

        //validate
        assertFalse(db.existsById(TableName.class, new TableName(7,8)));
    }

    @Before
    public void beforeDeleteAsync() {
        TableName obj = new TableName(9, 10);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.save(TableName.class, obj);
    }

    @Test
    public void testDeleteAsync() {
        TableName obj = new TableName(9, 10);
        db.deleteAsync(TableName.class, obj);
        
        //validate
        assertFalse(db.existsById(TableName.class, new TableName(9,10)));
    }

    @Before
    public void beforeExecuteOne() {
        TableName obj = new TableName(10, 11);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.save(TableName.class, obj);
    }

    @Test
    public void testExecuteOne() {
        TableName obj = db.executeOne(TableName.class, "select * from table_name where key_one = 10 and key_two = 11");
        
        //Validate
        assertEquals(obj.getColOne(),"ColumnOne");
        assertEquals(obj.getColTwo(),"ColumnTwo");
    }

    @Before
    public void beforeExecuteAll() {
        TableName obj1 = new TableName(12, 13);
        obj1.setColOne("ColumnOne");
        obj1.setColTwo("ColumnTwo");
        db.save(TableName.class, obj1);

        TableName obj2 = new TableName(14, 15);
        obj2.setColOne("ColumnOne - 2");
        obj2.setColTwo("ColumnTwo - 2");
        db.save(TableName.class, obj2);
    }

    @Test
    public void testExecuteAll() {
        List<TableName> objects = db.executeAll(TableName.class, "select * from table_name");
        
        //validate
        assertTrue(objects.size()>=2);
    }

    @Before
    public void beforeGetAllById() {
        TableName obj1 = new TableName(12, 13);
        obj1.setColOne("ColumnOne");
        obj1.setColTwo("ColumnTwo");
        db.save(TableName.class, obj1);

        TableName obj2 = new TableName(14, 15);
        obj2.setColOne("ColumnOne - 2");
        obj2.setColTwo("ColumnTwo - 2");
        db.save(TableName.class, obj2);
    }

    @Test
    public void testGetAllById() {
        List<TableName> objects = db.getAllById(TableName.class, new TableName(12,13));
        
        //validate
        assertTrue(objects.size()==1);
    }

    @After
    public void afterExecuteAll() {
        TableName obj1 = new TableName(12, 13);
        db.delete(TableName.class, obj1);

        TableName obj2 = new TableName(14, 15);
        db.delete(TableName.class, obj2);
    }
    
    @Before
    public void beforeExistsById() {
        db.save(TableName.class, new TableName(15, 17));
    }

    @Test
    public void testExistsById() {
        assertTrue(db.existsById(TableName.class,new TableName(15, 17)));
    }
    
    @Before
    public void beforeGetOneByID() {
        TableName obj = new TableName(18,19);
        obj.setColOne("ColumnOne");
        obj.setColTwo("ColumnTwo");
        db.save(TableName.class, obj);
    }

    @Test
    public void testGetOneByID() {
        TableName obj = new TableName(18);
        obj = db.getOneById(TableName.class, obj);
        
        //Validate
        assertEquals(obj.getColOne(),"ColumnOne");
        assertEquals(obj.getColTwo(),"ColumnTwo");

    }

    @After
    public void afterGetOneByID() {
        TableName obj = new TableName(18,19);
        db.delete(TableName.class, obj);
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
    

    @AfterClass
    public static void shutdown() {
        db.close();
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }
}
