/*
 * Copyright 2018 T-Mobile US, Inc.
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

import com.datastax.oss.driver.api.core.cql.Statement;

/**
 * Generic Failover Policy interface. The failover policy is called when a statement is executed and an exception is raised. If shouldFailover returns true then the query is reattempted on the failover profile.
 */
public abstract class FailoverPolicy {
    /**
     * Evaluates statement and exception to determine if failover is to occurr
     * @param exception exception received during execute
     * @param statement statement that was executed
     * @return boolean value to indicate failover
     */
    public abstract Boolean shouldFailover(Exception exception, Statement statement);
}
