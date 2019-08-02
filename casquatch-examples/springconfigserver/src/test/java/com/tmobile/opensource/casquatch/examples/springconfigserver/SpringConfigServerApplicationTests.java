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

package com.tmobile.opensource.casquatch.examples.springconfigserver;

import com.tmobile.opensource.casquatch.CasquatchDao;
import com.tmobile.opensource.casquatch.tests.podam.CasquatchPodamFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class SpringConfigServerApplicationTests {

	@Autowired
	private CasquatchDao casquatchDao;
	private CasquatchPodamFactoryImpl casquatchPodamFactory= new CasquatchPodamFactoryImpl();
	private CasquatchEnvironmentRepository casquatchEnvironmentRepository= new CasquatchEnvironmentRepository(casquatchDao);

	@Test
	public void testFindOne() {

		Configuration configuration = casquatchPodamFactory.manufacturePojoWithFullData(Configuration.class);
		casquatchDao.save(Configuration.class,configuration);

		casquatchEnvironmentRepository = new CasquatchEnvironmentRepository(casquatchDao);
		Environment environment = casquatchEnvironmentRepository.findOne(configuration.getApplication(),configuration.getProfile(),configuration.getLabel());

		assertEquals(configuration.getValue(),environment.getPropertySources().get(0).getSource().get(configuration.getKey()));

	}

	@Test
	public void testFindOneMissing() {

		Configuration configuration = casquatchPodamFactory.manufacturePojoWithFullData(Configuration.class);

		casquatchEnvironmentRepository = new CasquatchEnvironmentRepository(casquatchDao);
		Environment environment = casquatchEnvironmentRepository.findOne(configuration.getApplication(),configuration.getProfile(),configuration.getLabel());

		assertNull(environment.getPropertySources().get(0).getSource().get(configuration.getKey()));

	}

}
