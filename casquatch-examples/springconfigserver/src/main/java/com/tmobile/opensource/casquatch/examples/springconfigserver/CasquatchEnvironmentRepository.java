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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of EnvironmentRepository to back with Casquatch
 */
@SuppressWarnings("WeakerAccess")
public class CasquatchEnvironmentRepository implements EnvironmentRepository {

    private final static Logger logger = LoggerFactory.getLogger(CasquatchEnvironmentRepository.class);
	
	private final CasquatchDao db;
	
	/**
     * Initializes the Repository
     * @param db Initialized db
     */	
	public CasquatchEnvironmentRepository(CasquatchDao db) {
		this.db = db;		
	}
	
	/**
     * Implements findOne for repository interface
     * @param application application name
     * @param profile profile name
     * @param label optional label
     */			
	public Environment findOne(String application, String profile, String label) {
		logger.debug("Loading Configuration for "+application+"."+profile+"."+label);
		
		Environment environment = new Environment(application,profile);
		
		Configuration searchConfig = new Configuration();
		searchConfig.setApplication(application);
		searchConfig.setProfile(profile);
		searchConfig.setLabel(label);
			
		List<Configuration> configList = db.getAllById(Configuration.class, searchConfig);
		
		final Map<String, String> properties = new HashMap<>();
		configList.forEach((Configuration conf) -> properties.put(conf.getKey(), conf.getValue()));
        environment.add(new PropertySource(application+"-"+label, properties));
		return environment;
	}


}
