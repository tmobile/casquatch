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
import com.tmobile.opensource.casquatch.CassandraAdminDriver;
import com.tmobile.opensource.casquatch.CassandraDriver;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDriverDockerFilterTests {

   private static final Logger logger = LoggerFactory.getLogger(CassandraDriverDockerFilterTests.class);
	
   @BeforeClass
   public static void setUp() {
   }
   
   private Collection<Host> datacenterTest(List<String> datacenters) {
	   CassandraAdminDriver db = 
			   new CassandraAdminDriver(
					   CassandraDriver.builder()
					   .withContactPoints("localhost")
					   .withKeyspace("system")
					   .withDataCenters(datacenters)
					   .build()
						);
		   Collection<Host> hostList = db.getDatastaxSession().getCluster().getMetadata().getAllHosts();
		   db.close();
		   return hostList;
   }
   
   @Test
   public void testDatacenterFilterOne() {
	   List<String> datacenters = Arrays.asList("seed");
	   
	   Collection<Host> hostList = datacenterTest(datacenters);

	   for(Host host: hostList) {
		   if(datacenters.contains(host.getDatacenter())) {
			   logger.trace(host.getAddress()+" : "+host.getDatacenter()+" matched");
		   }
		   else {
			   fail("datacenter does not match filters "+host.getAddress()+" : "+host.getDatacenter());
		   }
	   }	
   }
   
   @Test
   public void testDatacenterFilterMulti() {
	   List<String> datacenters = Arrays.asList("seed","search");
	   
	   Collection<Host> hostList = datacenterTest(datacenters);

	   for(Host host: hostList) {
		   if(datacenters.contains(host.getDatacenter())) {
			   logger.trace(host.getAddress()+" : "+host.getDatacenter()+" matched");
		   }
		   else {
			   fail("datacenter does not match filters "+host.getAddress()+" : "+host.getDatacenter());
		   }
	   }
   }   

   @Test(expected=java.lang.IllegalArgumentException.class)
   public void testDatacenterFilterBad() {
	   List<String> datacenters = Arrays.asList("notadatacenter");
		   
	   Collection<Host> hostList = datacenterTest(datacenters);

	   for(Host host: hostList) {
		   if(datacenters.contains(host.getDatacenter())) {
			   logger.trace(host.getAddress()+" : "+host.getDatacenter()+" matched");
		   }
		   else {
			   fail("datacenter does not match filters "+host.getAddress()+" : "+host.getDatacenter());
		   }
	   }
   }
   
   private Collection<Host> workloadTest(List<String> workloads) {
	   CassandraAdminDriver db = 
			   new CassandraAdminDriver(
					   CassandraDriver.builder()
					   .withContactPoints("localhost")
					   .withKeyspace("system")
					   .withWorkloads(workloads)
					   .build()
						);
		   Collection<Host> hostList = db.getDatastaxSession().getCluster().getMetadata().getAllHosts();
		   db.close();
		   return hostList;
   }
   
   @Test
   public void testWorkloadFilterOne() {
	   List<String> workloads = Arrays.asList("Cassandra");
	   
	   Collection<Host> hostList = workloadTest(workloads);

	   for(Host host: hostList) {
		   if(workloads.contains(host.getDseWorkload())) {
			   logger.trace(host.getAddress()+" : "+host.getDseWorkload()+" matched");
		   }
		   else {
			   fail("workload does not match filters "+host.getAddress()+" : "+host.getDseWorkload());
		   }
	   }	
   }
   
   @Test
   public void testWorkloadFilterMulti() {
	   List<String> workloads = Arrays.asList("Cassandra","Search");
	   
	   Collection<Host> hostList = workloadTest(workloads);

	   for(Host host: hostList) {
		   if(workloads.contains(host.getDseWorkload())) {
			   logger.trace(host.getAddress()+" : "+host.getDseWorkload()+" matched");
		   }
		   else {
			   fail("workload does not match filters "+host.getAddress()+" : "+host.getDseWorkload());
		   }
	   }	
   }   

   @Test(expected=java.lang.IllegalArgumentException.class)
   public void testWorkloadFilterBad() {
	   List<String> workloads = Arrays.asList("NotAWorkload");
	   
	   Collection<Host> hostList = workloadTest(workloads);

	   for(Host host: hostList) {
		   if(workloads.contains(host.getDseWorkload())) {
			   logger.trace(host.getAddress()+" : "+host.getDseWorkload()+" matched");
		   }
		   else {
			   fail("workload does not match filters "+host.getAddress()+" : "+host.getDseWorkload());
		   }
	   }	
   }
}

