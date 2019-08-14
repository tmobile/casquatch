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

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration implementation to define Casquatch Beans
 */
@Configuration
public class CasquatchSpringBeans {

    private final CasquatchDao dao;

    /**
     * Initialize Spring Beans by creating DAO.
     */
    public CasquatchSpringBeans() {
        this.dao=new CasquatchDaoBuilder().build();
    }

    /**
     * Create a CQL session from initialized dao
     * @return cql session
     */
    @Bean
    public CqlSession cqlSession() {
        return this.casquatchDao().getSession();
    }

    /**
     * Return the constructed dao
     * @return casquatch dao
     */
    @Bean
    public CasquatchDao casquatchDao() {
        return this.dao;
    }

}