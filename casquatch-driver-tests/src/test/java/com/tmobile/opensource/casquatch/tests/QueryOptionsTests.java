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

package com.tmobile.opensource.casquatch.tests;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.tmobile.opensource.casquatch.QueryOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class QueryOptionsTests {

    @Test
    public void testImmutable() {
        QueryOptions queryOptions1 = new QueryOptions().withLimit(5);
        QueryOptions queryOptions2 = queryOptions1;
        queryOptions2=queryOptions2.withLimit(100);
        log.trace(queryOptions1.toString());
        log.trace(queryOptions2.toString());
        assert(!queryOptions1.equals(queryOptions2));
    }

    @Test
    public void testLimit() {
        QueryOptions queryOptions = new QueryOptions().withLimit(5);

        assert (5 == queryOptions.getLimit());
        assertNull(queryOptions.withoutLimit().getLimit());
    }

    @Test
    public void testNulls() {
        QueryOptions queryOptions = new QueryOptions();
        assertTrue(queryOptions.withPersistNulls().getPersistNulls());
        assertFalse(queryOptions.withoutPersistNulls().getPersistNulls());
    }

    @Test
    public void testConsistencyLevel() {
        QueryOptions queryOptions = new QueryOptions().withConsistencyLevel("LOCAL_QUORUM");
        assertEquals(queryOptions.getConsistencyLevel(), DefaultConsistencyLevel.LOCAL_QUORUM);
    }

    @Test
    public void testProfile() {
        QueryOptions queryOptions = new QueryOptions().withProfile("testing");
        assertEquals(queryOptions.getProfile(),"testing");
        assertNull(queryOptions.withoutProfile().getProfile());
    }

    @Test
    public void testTTL() {
        QueryOptions queryOptions = new QueryOptions().withTTL(100);
        assert(100==queryOptions.getTtl());
        assertNull(queryOptions.withoutTTL().getTtl());
    }







}
