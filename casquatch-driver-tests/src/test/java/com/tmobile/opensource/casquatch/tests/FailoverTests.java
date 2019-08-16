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

import com.tmobile.opensource.casquatch.CasquatchDao;
import com.tmobile.opensource.casquatch.CasquatchTestDaoBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class FailoverTests {

    private static CasquatchDao casquatchDao;

    @BeforeClass
    public static void setUp() {
        casquatchDao=new CasquatchTestDaoBuilder()
                .withEmbedded()
                .withTestKeyspace("junittest")
                .withDDL(SimpleTable.getDDL())
                .withBasicRequestConsistency("TWO")
                .startProfile("test_one")
                .withBasicRequestConsistency("ONE")
                .endProfile()
                .withFailoverPolicyProfile("test_one")
                .build();
    }

    public CasquatchDao getCasquatchDao() {
        return casquatchDao;
    }

    @Test
    public void testFailover() {
        casquatchDao.save(SimpleTable.class, new SimpleTable(1,1));
    }

    @Test
    public void testFailoverASync() throws InterruptedException {
        CompletableFuture<Void> completableFuture = casquatchDao.saveAsync(SimpleTable.class, new SimpleTable(1,1));
        int maxWait=10;
        for(int i=0;i<=maxWait;i++) {
            if(completableFuture.isDone()) {
                break;
            }
            Thread.sleep(1000);
        }
    }

}
