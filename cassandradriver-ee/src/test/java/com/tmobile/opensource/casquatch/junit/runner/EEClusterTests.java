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
package com.tmobile.opensource.casquatch.junit.runner;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import com.tmobile.opensource.casquatch.junit.CassandraDriverEEDockerFilterTests;

public class EEClusterTests {
	
	public static void main(String[] args) throws Exception {       
		JUnitCore junitCore = new JUnitCore();
		Result result = junitCore.run(CassandraDriverEEDockerFilterTests.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());	            
        }
        System.out.println("EECluster Tests:  Ran "+result.getRunCount()+" tests with "+result.getFailureCount()+" failures.");
        System.exit(0);	  
     
	}

}
