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
import com.datastax.driver.core.policies.HostFilterPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.google.common.base.Predicate;
import com.tmobile.opensource.casquatch.exceptions.DriverException;

public class WorkloadFilterPolicy extends HostFilterPolicy{
    
    /**
     * Placeholder. Functionality only available in Casquatch-EE
     * @param childPolicy the wrapped policy
     * @param predicate a defined host pridicate
     */
    private WorkloadFilterPolicy(LoadBalancingPolicy childPolicy, Predicate<Host> predicate) {
      super(childPolicy, predicate);
      throw new DriverException(402,"Workload Filter Policy requires Casquatch-EE");
    }

    /**
     * Placeholder. Functionality only available in Casquatch-EE     *
     * @param childPolicy the wrapped policy.
     * @param workloads List of workloads which must match to be passed through
     */
    public static WorkloadFilterPolicy fromWorkloadList(LoadBalancingPolicy childPolicy, Iterable<String> workloads) {
        throw new DriverException(402,"Workload Filter Policy requires Casquatch-EE");
    }
}


