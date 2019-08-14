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

package com.tmobile.opensource.casquatch;

import lombok.Getter;

import java.io.InputStream;
import java.util.Properties;

/**
 * Class to hold maven coordinates
 */
@Getter
public class CasquatchCoordinations {
    String version;
    String groupId;
    String artifactId;
    String name;

    /**
     * Empty constructor to default to maven.properties
     */
    public CasquatchCoordinations() {
        this("/maven.properties");
    }

    /**
     * Constructor that takes a path to load
     * @param path path to load
     */
    public CasquatchCoordinations(String path) {
        try (InputStream inputStream = CasquatchCoordinations.class.getResourceAsStream(path)){
            Properties properties = new Properties();
            properties.load(inputStream);
            this.version=properties.getProperty("version");
            this.groupId=properties.getProperty("groupId");
            this.artifactId=properties.getProperty("artifactId");
            this.name=properties.getProperty("name");
        }
        catch (Exception e) {
            throw new DriverException(e);
        }
    }

    /**
     * String representation
     * @return Name Version
     */
    public String toString() {
        return this.getName()+" "+this.getVersion();

    }

}
