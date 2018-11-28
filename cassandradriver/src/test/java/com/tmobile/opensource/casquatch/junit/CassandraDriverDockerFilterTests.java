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

import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.tmobile.opensource.casquatch.CassandraAdminDriver;
import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.exceptions.DriverException;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDriverDockerFilterTests {

   private static final Logger logger = LoggerFactory.getLogger(CassandraDriverDockerFilterTests.class);
	
   @BeforeClass
   public static void setUp() {
   }
   
   private Boolean datacenterTest(List<String> datacenters) {
	   CassandraAdminDriver db = 
			   new CassandraAdminDriver(
					   CassandraDriver.builder()
					   .withContactPoints("localhost")
					   .withKeyspace("system")
					   .withDataCenters(datacenters)
					   .build()
						);
		   List<Host> hostList = db.getDatastaxSession().getCluster().getMetadata().getAllHosts().stream().filter(h -> db.getDatastaxSession().getCluster().getConfiguration().getPolicies().getLoadBalancingPolicy().distance(h) != HostDistance.IGNORED).collect(Collectors.toList());
		   db.close();
		   
		   for(Host host: hostList) {			   
			   if(datacenters.contains(host.getDatacenter())) {
				   logger.trace(host.getAddress()+" : "+host.getDatacenter()+" matched");
			   }
			   else {
				   logger.error(host.getAddress()+" : "+host.getDatacenter()+" not matched. Expected "+datacenters.toString());
				   return false;
			   }
		   }
		   return true;
   }
   
   @Test
   public void testDatacenterFilterOne() {
	   List<String> datacenters = Arrays.asList("dc1");
	   
	   if(!datacenterTest(datacenters)) {
		   fail("datacenter test failed for "+datacenters.toString());
	   }
   }
   
   @Test
   public void testDatacenterFilterMulti() {
	   List<String> datacenters = Arrays.asList("dc1","dc2");
	   
	   if(!datacenterTest(datacenters)) {
		   fail("datacenter test failed for "+datacenters.toString());
	   }
   }   

   @Test(expected=DriverException.class)
   public void testDatacenterFilterBad() {
	   List<String> datacenters = Arrays.asList("notadatacenter");
		   
	   if(datacenterTest(datacenters)) {
		   fail("datacenter test failed for "+datacenters.toString());
	   }
   }
}

