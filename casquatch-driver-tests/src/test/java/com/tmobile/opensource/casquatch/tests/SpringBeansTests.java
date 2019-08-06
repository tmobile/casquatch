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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.tmobile.opensource.casquatch.CasquatchDao;
import com.tmobile.opensource.casquatch.CasquatchTestDaoBuilder;
import com.tmobile.opensource.casquatch.annotation.CasquatchSpring;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@CasquatchSpring
@SpringBootApplication
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@TestConfiguration
public class SpringBeansTests {

    @Autowired
    CasquatchDao casquatchDao;

    @Autowired
    CqlSession session;

    @BeforeClass
    public static void setup() {
        new CasquatchTestDaoBuilder().withEmbedded().withTestKeyspace("junittest").buildSpring();
    }

    @Test
    public void testAutowire() {
        assertNotNull(casquatchDao);
        assertNotNull(session);
    }

    @Test
    public void testSession() {
        assert(session.equals(casquatchDao.getSession()));
    }

    @Test
    public void testQuery() {
        Row row = session.execute("select release_version from system.local").one();
        assertNotNull(row);
        assertNotNull(row.getString("release_version"));
    }

}
