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
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDriverEEDockerFilterTests extends CassandraDriverDockerFilterTests {

   private static final Logger logger = LoggerFactory.getLogger(CassandraDriverEEDockerFilterTests.class);
      
   private Boolean workloadTest(List<String> workloads) {
	   CassandraAdminDriver db = 
			   new CassandraAdminDriver(
					   CassandraDriver.builder()
					   .withContactPoints("localhost")
					   .withKeyspace("system")
					   .withWorkloads(workloads)
					   .build()
						);
		   List<Host> hostList = db.getDatastaxSession().getCluster().getMetadata().getAllHosts().stream().filter(h -> db.getDatastaxSession().getCluster().getConfiguration().getPolicies().getLoadBalancingPolicy().distance(h) != HostDistance.IGNORED).collect(Collectors.toList());
		   db.close();
		   for(Host host: hostList) {
			   if(host.getDseWorkloads().stream().anyMatch(workloads :: contains)) {
				   logger.trace(host.getAddress()+" : "+host.getDseWorkloads()+" matched");
			   }
			   else {
				   logger.error("workload does not match filters "+host.getAddress()+" has "+host.getDseWorkloads()+" not matched.  Expetected "+workloads.toString());
				   return false;
			   }
		   }	
		   return true;
   }
   
   
   @Test
   public void testWorkloadFilterOne() {
	   
	   List<String> workloads = Arrays.asList("Search");
	   
	   if(!workloadTest(workloads)) {
		   fail("Workload test failed to filter for "+workloads.toString());
	   }
   }
   
   @Test
   public void testWorkloadFilterMulti() {
	   List<String> workloads = Arrays.asList("Cassandra","Search");
	   
	   if(!workloadTest(workloads)) {
		   fail("Workload test failed to filter for "+workloads.toString());
	   }
   } 

   @Test(expected=DriverException.class)
   public void testWorkloadFilterBad() {
   	   List<String> workloads = Arrays.asList("NotAWorkload");
	   
	   if(workloadTest(workloads)) {
		   fail("Workload test was ailed to filter for "+workloads.toString());
	   }
   }
}

