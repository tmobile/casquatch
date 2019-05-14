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

package com.tmobile.opensource.casquatch.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.opensource.casquatch.junit.podam.CassandraDriverPodamFactoryImpl;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.jemos.podam.api.PodamFactory;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


public abstract class AbstractCassandraDriverTableTests <T extends AbstractCassandraTable>{

    protected final static Logger logger = LoggerFactory.getLogger(AbstractCassandraDriverTableTests.class);

    private static PodamFactory podamFactory = new CassandraDriverPodamFactoryImpl();

    protected abstract Class<T> getTableClass();

    @Test
    public void serialize() throws IOException {
        T object = podamFactory.manufacturePojoWithFullData(this.getTableClass());

        T deserializedObject = new ObjectMapper().readValue(object.toString(), this.getTableClass());

        assertTrue(object.equals(deserializedObject));

    }

}

