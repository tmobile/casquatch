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
import com.tmobile.opensource.casquatch.models.junittest.TableName;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

public class CassandraDriverDockerSSLTests extends CassandraDriverTestSuite {

    @BeforeClass
    public static void setUp() throws IOException, TTransportException {
    	
    	CassandraDriver.Builder builder = new CassandraDriver.Builder()
        		.withContactPoints("localhost")
        		.withLocalDC("dc1")
        		.withoutDriverConfig()
        		.withSSL()
        		.withTrustStore("../config/client.truststore", "cassandra")
        		.withPort(9142);
    	
    	db = builder.withKeyspace("system").build();
        createSchema();
        db.close();
        db = builder.withKeyspace("junittest").build();
    }
}
