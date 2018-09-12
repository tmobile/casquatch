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

import com.tmobile.opensource.casquatch.CassandraDriver;
import org.junit.BeforeClass;

public class CassandraDriverDockerTests extends CassandraDriverTestSuite {

    @BeforeClass
    public static void setUp() {

    	CassandraDriver.Builder builder = new CassandraDriver.Builder()
        		.withContactPoints("localhost")
        		.withLocalDC("dc1")
        		.withoutDriverConfig()
        		.withReadTimeout(30000)
        		.withPort(9042);
        
        db = builder.withKeyspace("system").build();
        createSchema();
        db.close();
        
        db = builder.withKeyspace("junittest").build();
    }
}
