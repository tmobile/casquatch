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

import com.typesafe.config.Optional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration object for LoadTest app
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Slf4j
public class LoadTestConfig {
    private Boolean doRead = false;
    private Boolean doWrite = false;
    private Boolean doCheck = false;
    private Integer delay=0;
    private Integer loops=1;
    private List<String> entities=new ArrayList<>();

    @Optional
    private List<Class> entityClasses=new ArrayList<>();

    /**
     * Convert the entity string list to class list
     * @return list of entity classes
     */
    public List<Class> getEntityClasses() {
        if(entityClasses.size()==0) {
            for (String entity : this.entities) {
                Class clazz;
                try {
                    if (!entity.contains(".")) {
                        clazz = Class.forName(LoadTestApplication.class.getPackage().getName() + "." + entity);
                    } else {
                        clazz = Class.forName(entity);
                    }
                    entityClasses.add(clazz);
                    log.info("Found class "+clazz.toString());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return entityClasses;
    }
}
