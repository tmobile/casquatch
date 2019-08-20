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

package com.tmobile.opensource.casquatch.generator;

import com.tmobile.opensource.casquatch.DriverException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.Optional;
import lombok.*;

import java.util.List;

/**
 * Configuration object to be populated from reference files by  {@link ConfigBeanFactory#create(Config, Class)}
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CasquatchGeneratorConfiguration {
    @Optional
    String username;
    @Optional
    String password;
    @Optional
    String keyspace;
    @Optional
    String datacenter;
    @Optional
    List<String> contactPoints;
    @Optional
    List<String> tables;
    @Optional
    List<String> types;
    @Optional
    Boolean console = false;
    @Optional
    Boolean file = false;
    @Optional
    String outputFolder;
    @Optional
    Boolean createPackage=false;
    @Optional
    String packageName;
    @Optional
    Boolean overwrite=false;
    @Optional
    Boolean minify=false;
    @Optional
    Boolean createTests=true;

    /**
     * Validate the configuration and throw an exception on failure
     * @throws DriverException exception for invalid configuration
     */
    public void validate() throws DriverException {
//        if(this.getKeyspace()==null || (this.getKeyspace()!=null && this.getKeyspace().isEmpty())) {
//            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION,"Keyspace is required");
//        }
//        if(this.getDatacenter()==null || (this.getDatacenter()!=null && this.getDatacenter().isEmpty())) {
//            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION,"Datacenter is required");
//        }
//        if(this.getContactPoints()==null || (this.getContactPoints()!=null && this.getContactPoints().isEmpty())) {
//            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION,"ContactPoints are required");
//        }
        if(this.getFile() && this.getOutputFolder()==null) {
            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION,"outputFolder is required when file=true");
        }
        if(!this.getFile() && !this.getConsole()) {
            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION,"Either console or file must be set to true");
        }
    }
}
