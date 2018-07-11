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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CassandraDriverDSETests {

    private static CassandraDriver db;
    private final static Logger logger = LoggerFactory.getLogger(CassandraDriverDSETests.class);

    @BeforeClass
    public static void setUp() throws IOException, TTransportException {
    	
    	/*
    	 * This assumes that thte local docker has solr running with a core created on table_name
    	 */
        db = new CassandraDriver.Builder()
        		.withContactPoints("localhost")
        		.withLocalDC("dc1")
        		.withKeyspace("junittest")
        		.withSolr()
        		.withSolrDC("dc1")
        		.withoutDriverConfig()
        		.build();
    }

    @Before
    public void beforeGetAllBySolar() throws InterruptedException {
        TableName obj1 = new TableName(14, 1);
        obj1.setColOne("test");
        obj1.setColTwo("ColumnTwo");
        db.save(TableName.class, obj1);

        TableName obj2 = new TableName(14, 2);
        obj2.setColOne("test");
        obj2.setColTwo("ColumnTwo - 2");
        db.save(TableName.class, obj2);

        TableName obj3 = new TableName(14, 3);
        obj3.setColOne("test 2");
        obj3.setColTwo("ColumnTwo - 2");
        db.save(TableName.class, obj3);
        Thread.sleep(30000);
    }

    @Test
    public void testGetAllBySolr() {
    	List<TableName> results = db.getAllBySolr(TableName.class,"{'q': '*:*', 'fq': 'col_one: test'}");
    	logger.debug("Found: "+results.size());
    	assert(results.size()>0);

    }

    @AfterClass
    public static void shutdown() {
        db.close();
    }
}
