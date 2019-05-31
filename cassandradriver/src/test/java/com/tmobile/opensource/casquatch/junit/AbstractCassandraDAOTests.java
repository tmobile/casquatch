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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.junit.podam.CassandraDriverPodamFactoryImpl;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;
import com.tmobile.opensource.casquatch.dao.AbstractCassandraDAO;
import com.tmobile.opensource.casquatch.dao.Request;
import com.tmobile.opensource.casquatch.dao.Response;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.co.jemos.podam.api.PodamFactory;

import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractCassandraDAOTests <T extends AbstractCassandraTable, S extends AbstractCassandraDAO<T>>{

    protected static  PodamFactory podamFactory = new CassandraDriverPodamFactoryImpl();
    protected final static Logger logger = LoggerFactory.getLogger(AbstractCassandraDAOTests.class);

    protected abstract S getService();
    protected abstract Class<S> getServiceClass();
    protected abstract Class<T> getTableClass();
    protected abstract MockMvc getMockMvc();

    protected CassandraDriver getDB() {
        return this.getService().getDB();
    };

    private Response<T> deserialize(String response) {
        ObjectMapper mapper = new ObjectMapper();

        JavaType type = mapper.getTypeFactory().constructParametricType(Response.class, this.getTableClass());
        try {
            return mapper.readValue(response, type);
        } catch (IOException e) {
            logger.error("Unable to deserialize response",e);
            return new Response<T>(Response.Status.ERROR);
        }
    }

    private String getApi(String method) {
        try {
            return
                this.getServiceClass().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class).value()[0]+
                this.getServiceClass().getMethod(method,Request.class).getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class).value()[0];
        } catch (NoSuchMethodException e) {
            logger.error("Unable to load API",e);
            return null;
        }
    }

    @Test
    public void testGet() throws Exception {

        T payload = podamFactory.manufacturePojoWithFullData(this.getTableClass());
        Request<T> request = new Request<>(payload);
        Response<T> response = new Response<>(payload);

        logger.trace("Payload: {}",payload.toString());
        logger.trace("Request: {}",request.toString());
        logger.trace("Expected Response: {}",response.toString());

        this.getDB().save(this.getTableClass(),payload);

        given(this.getService().get(request)).willReturn(response);

        String api = getApi("get");
        logger.trace("API: {}",api);

        MvcResult result = this.getMockMvc().perform(post(api)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andReturn();

        Response<T> actualResponse = deserialize(result.getResponse().getContentAsString());

        logger.trace("Expected Response: {}",response.toString());
        logger.trace("Actual Response: {}",actualResponse.toString());

        assertTrue(response.getStatus().equals(actualResponse.getStatus()));
        assertTrue(response.getPayload().get(0).equals(actualResponse.getPayload().get(0)));
        this.getDB().delete(this.getTableClass(),payload);

    }

    @Test
    public void testSave() throws Exception {

        T payload = podamFactory.manufacturePojoWithFullData(this.getTableClass());
        Request<T> request = new Request<>(payload);
        Response<T> response = new Response<>(Response.Status.SUCCCES);

        logger.trace("Payload: {}",payload.toString());
        logger.trace("Request: {}",request.toString());
        logger.trace("Response: {}",response.toString());

        given(this.getService().save(request)).willReturn(response);

        String api = getApi("save");

        logger.trace("API: {}",api);

        MvcResult result = this.getMockMvc().perform(post(api)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andReturn();

        Response<T> actualResponse = deserialize(result.getResponse().getContentAsString());

        logger.trace("Expected Response: {}",response.toString());
        logger.trace("Actual Response: {}",actualResponse.toString());

        assertTrue(response.getStatus().equals(actualResponse.getStatus()));
        assertNull(actualResponse.getPayload());

        this.getDB().delete(this.getTableClass(),payload);

    }

}

