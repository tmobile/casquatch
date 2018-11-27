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

package com.tmobile.opensource.casquatch.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import com.tmobile.opensource.casquatch.generator.CassandraGenerator;

public class Application implements CommandLineRunner {

	private final static Logger logger = LoggerFactory.getLogger(Application.class);
	
	@Autowired
    private ApplicationArguments  applicationArguments;

	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		if(applicationArguments.containsOption("help") || applicationArguments.getOptionNames().size() == 0) {
			CassandraGenerator.help();
		}
		else {			
			CassandraGenerator generatorController = CassandraGenerator
                    .builder()
                    .withArgs(applicationArguments)
                    .build();			
			
			generatorController.run();
		}
		System.exit(0);
	}
}

	
