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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.policies.ChainableLoadBalancingPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericFilterPolicy implements ChainableLoadBalancingPolicy {
	
	private static final Logger logger = LoggerFactory.getLogger(DCFilterPolicy.class);

	private final LoadBalancingPolicy childPolicy;
	
	protected List<String> filters;
	
	/**
	* Generic Filter Policy constructor
	* @param childPolicy Child load balancing policy
	* @param filters list of filters
	* @return Reference to Builder object
	*/
	protected GenericFilterPolicy(
		LoadBalancingPolicy childPolicy,
		List<String> filters) {
		this.childPolicy = childPolicy;
		this.filters = filters != null ? filters : new ArrayList<String>();
	}
	
	/**
	* Gets the child policy
	* @return child policy
	*/
	@Override
	public LoadBalancingPolicy getChildPolicy() {
		return childPolicy;
	}   
	
	/**
	* Initializes the cluster by filtering the list of hosts and passing to child policy
	* @param cluster Reference to cluster
	* @param hosts Collection of hosts
	*/
	@Override
	public void init(Cluster cluster, Collection<Host> hosts) {
		logger.trace("Initializing cluster with the following filters: "+this.filters.toString());		  
	
		List<Host> allowedHosts = new ArrayList<Host>(hosts.size()); 
		
		
		hosts.forEach((h)->{if(allowHost(h)) {allowedHosts.add(h);}});
		
		
		if (allowedHosts.isEmpty()) {
			throw new IllegalArgumentException(String.format( "No hosts remain after filter applied (%s)", hosts));
		}
		
		childPolicy.init(cluster, allowedHosts);
		
	}
	
	/**
	* Abstract method to apply filters and determine if host is allowed
	* @param host host to check
	* @param boolean for allowed
	*/
	protected abstract Boolean allowHost(Host host);
	
	/**
	* Returns the HostDistance for the provided host. If host is allowed them it returns the distance from the childPolicy. If not then returns IGNORED
	*
	* @param host the host of which to return the distance of.
	* @return the HostDistance to the host as returned by the child policy.
	*/
	@Override
	public HostDistance distance(Host host) {
		return allowHost(host) ? childPolicy.distance(host) :  HostDistance.IGNORED;
	}
	
	/**
	* Calculate the query play according to the child policy
	*
	* @param loggedKeyspace the currently logged keyspace.
	* @param statement the statement for which to build the plan.
	* @return the new query plan.
	*/
	@Override
	public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {
		return childPolicy.newQueryPlan(loggedKeyspace, statement);
	}  
	
	/**
	* Call childPolicy onUp if filter passes
	* @param host Reference to host
	*/
	@Override
	public void onUp(Host host) {
		if (allowHost(host)) childPolicy.onUp(host);
	}
	
	/**
	* Call childPolicy onDown if filter passes
	* @param host Reference to host
	*/
	@Override
	public void onDown(Host host) {
		if (allowHost(host)) childPolicy.onDown(host);
	}
	
	/**
	* Call childPolicy onAdd if filter passes
	* @param host Reference to host
	*/
	@Override
	public void onAdd(Host host) {
		if (allowHost(host)) childPolicy.onAdd(host);
	}
	/**
	* Call childPolicy onRemove if filter passes
	* @param host Reference to host
	*/
	@Override
	public void onRemove(Host host) {
		if (allowHost(host)) childPolicy.onRemove(host);
	}    
	
	/**
	* Helper builder object to create a filter policy
	*/
	public abstract static class Builder {
	
		protected final LoadBalancingPolicy childPolicy;
		protected List<String> filters;
		
		/**
		* Creates a new filter policy builder given the child policy that the resulting policy
		* wraps.
		*
		* @param childPolicy the load balancing policy to filter
		*/
		public Builder(LoadBalancingPolicy childPolicy) {
			this.childPolicy = childPolicy;
			this.filters = new ArrayList<String>();
		}
		
		/**
		* Add all filters
		* @param filters for the policy
		* @return this builder.
		*/
		public Builder withFilters(List<String> filters) {
			logger.trace("Adding filters "+filters.toString());	
			this.filters.addAll(filters);
			return this;
		}
		
		/**
		* Add a filter to the policy
		* @param filter for the policy
		* @return this builder.
		*/
		public Builder withFilter(String filter) {
			logger.trace("Adding filter "+filter);	
			this.filters.add(filter);
			return this;
		}
		
		/**
		* Builds a new filter policy using the options set on this builder.
		*
		* @return the newly created filter policy
		*/
		public abstract LoadBalancingPolicy build();
	}
	
	/**
	* Call childPolicy close
	*/
	@Override
	public void close() {
		childPolicy.close();
	}
}