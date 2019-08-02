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

package com.tmobile.opensource.casquatch.examples.loadtest;

import com.tmobile.opensource.casquatch.CasquatchDao;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Demo Load Test Application. Receives configuration then produces the given load.
 */
@Slf4j
public class LoadTestApplication {

	public static void main(String[] args) {

		//Create CasquatchDao from config
		CasquatchDao db=CasquatchDao.builder().build();

		//Load loadtest config
		ConfigFactory.invalidateCaches();
		Config config = ConfigFactory.load().getConfig("loadtest");
		if(log.isTraceEnabled()) {
			for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
				log.trace("Config: {} -> {}", entry.getKey(), entry.getValue().render());
			}
		}
		LoadTestConfig loadTestConfig = ConfigBeanFactory.create(config,LoadTestConfig.class);

		//Run for each entity
		if(loadTestConfig.getEntities().size()>0) {
			for (Class entity : loadTestConfig.getEntityClasses()) {
				new LoadWrapper<>(entity, db).run(loadTestConfig);
			}
		}
		System.exit(0);
	}


}
