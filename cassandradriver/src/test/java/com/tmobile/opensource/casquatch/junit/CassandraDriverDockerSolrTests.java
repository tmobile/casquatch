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

import com.tmobile.opensource.casquatch.CassandraAdminDriver;
import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.exceptions.DriverException;
import com.tmobile.opensource.casquatch.models.junittest.TableName;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;

public class CassandraDriverDockerSolrTests extends CassandraDriverTestSuite {

    @BeforeClass
    public static void setUp(){
        
        CassandraDriver.Builder builder = new CassandraDriver.Builder()
        		.withContactPoints("localhost")
        		.withLocalDC("dc1")
        		.withSolr()
        		.withSolrDC("dc1")
        		.withoutDriverConfig()
        		.withReadTimeout(30000)
        		.withPort(9042);
        
        db = builder.withKeyspace("system").build();
        createSchema();
        db.close();
        
        db = builder.withKeyspace("junittest").build();
		db.execute("CREATE SEARCH INDEX IF NOT EXISTS ON junitTest.table_name;");
    }
    
    @Before
    public void beforeGetAllBySolr() {
        TableName obj1 = new TableName(15, 1);
        obj1.setColOne("test");
        obj1.setColTwo("ColumnTwo");
        db.save(TableName.class, obj1);

        TableName obj2 = new TableName(15, 2);
        obj2.setColOne("test");
        obj2.setColTwo("ColumnTwo - 2");
        db.save(TableName.class, obj2);

        TableName obj3 = new TableName(15, 3);
        obj3.setColOne("test 2");
        obj3.setColTwo("ColumnTwo - 2");
        db.save(TableName.class, obj3);
    }

    @Test
    public void testGetAllBySolr() throws InterruptedException {

    	int lc=0;
    	List<TableName> results;
    	do {
    		results = db.getAllBySolr(TableName.class,"{'q': '*:*', 'fq': 'col_one: test'}");
    		lc++;
    		Thread.sleep(5000);
    	} while (!(results.size()>0 | lc > 10));
    	
    	logger.debug("testGetAllBySolr Found: "+results.size());
    	assert(results.size()>0);

    }

    @Test
    public void testGetAllBySolrObject() throws InterruptedException {
    	
    	//Verify that the version is at least 6.0
    	CassandraAdminDriver adminDB = new CassandraAdminDriver(db);
    	Integer dseVersion = Integer.parseInt(adminDB.getDatastaxSession().execute("select dse_version from system.local").one().getString(0).split("\\.")[0]);
    	adminDB.close();
    	Assume.assumeTrue(dseVersion >= 6);
        
        //Run Test
    	int lc=0;
    	
    	TableName tmp = new TableName();
    	tmp.setColOne("test");
    	
    	List<TableName> results;
    	do {    		
			results = db.getAllBySolr(TableName.class,tmp);
    		lc++;
    		Thread.sleep(5000);
    	} while (!(results.size()>0 | lc > 10));
    	
    	logger.debug("testGetAllBySolrObject Found: "+results.size());
    	assert(results.size()>0);

    }

    @Test
    public void testGetAllBySolrCQL() throws InterruptedException {    	

    	//Verify that the version is at least 6.0
    	CassandraAdminDriver adminDB = new CassandraAdminDriver(db);
    	Integer dseVersion = Integer.parseInt(adminDB.getDatastaxSession().execute("select dse_version from system.local").one().getString(0).split("\\.")[0]);
    	adminDB.close();
    	Assume.assumeTrue(dseVersion >= 6);

    	int lc=0;
    	
    	List<TableName> results;
    	do {    		
			results = db.getAllBySolrCQL(TableName.class,"select * from table_name where col_one like 'test%'");
    		lc++;
    		Thread.sleep(5000);
    	} while (!(results.size()>0 | lc > 10));
    	
    	logger.debug("testGetAllBySolrObject Found: "+results.size());
    	assert(results.size()>0);

    }
    
    
}
