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

package com.tmobile.opensource.casquatch.examples.springrest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.opensource.casquatch.CasquatchDao;
import com.tmobile.opensource.casquatch.rest.Request;
import com.tmobile.opensource.casquatch.rest.Response;
import com.tmobile.opensource.casquatch.tests.podam.CasquatchPodamFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.co.jemos.podam.api.PodamFactory;

import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class SpringrestApplicationTests {
	@Autowired
	protected MockMvc mvc;

	@Autowired
	@Spy
	protected TableName_RestDao service;

	@Autowired
	protected CasquatchDao casquatchDao;

	protected static PodamFactory podamFactory = new CasquatchPodamFactoryImpl();


	private <T> Response<T> deserialize(Class<T> c, String response) {
		ObjectMapper mapper = new ObjectMapper();

		JavaType type = mapper.getTypeFactory().constructParametricType(Response.class, c);
		try {
			return mapper.readValue(response, type);
		} catch (IOException e) {
			log.error("Unable to deserialize response",e);
			return new Response<T>(Response.Status.ERROR);
		}
	}

	private String getApi(String method) {
		try {
			return
					service.getClass().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class).value()[0]+
					service.getClass().getMethod(method, Request.class).getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class).value()[0];
		} catch (NoSuchMethodException e) {
			log.error("Unable to load API",e);
			return null;
		}
	}

	@Test
	public void testGet() throws Exception {

		TableName payload = podamFactory.manufacturePojoWithFullData(TableName.class);
		Request<TableName> request = new Request<>(payload);
		Response<TableName> response = new Response<>(payload);

		log.trace("Payload: {}",payload.toString());
		log.trace("Request: {}",request.toString());
		log.trace("Expected Response: {}",response.toString());

		this.casquatchDao.save(TableName.class,payload);

		String api = getApi("get");
		log.trace("API: {}",api);

		MvcResult result = this.mvc.perform(post(api)
				.contentType(MediaType.APPLICATION_JSON)
				.content(request.toString()))
				.andExpect(status().isOk())
				.andReturn();

		Response<TableName> actualResponse = deserialize(TableName.class,result.getResponse().getContentAsString());

		log.trace("Expected Response: {}",response.toString());
		log.trace("Actual Response: {}",actualResponse.toString());

		assertTrue(response.getStatus().equals(actualResponse.getStatus()));
		assertTrue(response.getPayload().get(0).equals(actualResponse.getPayload().get(0)));
		this.casquatchDao.delete(TableName.class,payload);

	}

	@Test
	public void testSave() throws Exception {

		TableName payload = podamFactory.manufacturePojoWithFullData(TableName.class);
		Request<TableName> request = new Request<>(payload);
		Response<Void> response = new Response<>(Response.Status.NO_DATA_FOUND);

		log.trace("Payload: {}",payload.toString());
		log.trace("Request: {}",request.toString());
		log.trace("Response: {}",response.toString());

		String api = getApi("save");

		log.trace("API: {}",api);

		MvcResult result = mvc.perform(post(api)
				.contentType(MediaType.APPLICATION_JSON)
				.content(request.toString()))
				.andExpect(status().isOk())
				.andReturn();

		Response<Void> actualResponse = deserialize(Void.class, result.getResponse().getContentAsString());

		log.trace("Expected Response: {}",response.toString());
		log.trace("Actual Response: {}",actualResponse.toString());

		assertTrue(response.getStatus().equals(actualResponse.getStatus()));
		assertNull(actualResponse.getPayload());

		this.casquatchDao.delete(TableName.class,payload);

	}

}
