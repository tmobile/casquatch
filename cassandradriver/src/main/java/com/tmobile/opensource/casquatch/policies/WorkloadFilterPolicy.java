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
package com.tmobile.opensource.casquatch.policies;

import com.datastax.driver.core.Host;
import com.datastax.driver.core.policies.LoadBalancingPolicy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkloadFilterPolicy extends GenericFilterPolicy {

	private static final Logger logger = LoggerFactory.getLogger(WorkloadFilterPolicy.class);
	
	/**
	* Workload Filter policy constructor
	* @param childPolicy wrapped policy
	* @param filters list of filters to apply
	*/
	private WorkloadFilterPolicy(
		LoadBalancingPolicy childPolicy,
		List<String> filters) {
		super(childPolicy,filters);
	}
	
	/**
	* helper function for builder
	* @param childPolicy wrapped policy
	* @return Builder object
	*/
	public static Builder builder(LoadBalancingPolicy childPolicy) {
		return new Builder(childPolicy);
	} 
	
	/**
	* Filters hosts
	* @param host host to check
	* @param boolean for allowed
	*/
	@SuppressWarnings("deprecation")
	protected Boolean allowHost(Host host) {
		if(this.filters.size()==0) {
			this.filters.add(host.getDseWorkload());
		}
		if(this.filters.contains(host.getDseWorkload())) {    	  
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	* Helper builder object to create a workload aware policy.
	*/
	public static class Builder extends GenericFilterPolicy.Builder {
	
		/**
		* Basic constructor. Calls parent
		* @param childPolicy wrapped policy
		*/
		public Builder(LoadBalancingPolicy childPolicy) {
			super(childPolicy);
		}
		
		/**
		* Builds a new filter policy using the configured options
		* @return the new policy object
		*/
		public WorkloadFilterPolicy build() {
			return new WorkloadFilterPolicy(childPolicy, filters);
		}
	}

}


