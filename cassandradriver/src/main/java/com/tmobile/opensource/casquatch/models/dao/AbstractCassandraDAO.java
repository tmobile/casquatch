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

package com.tmobile.opensource.casquatch.models.dao;

import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Abstract Cassandra DAO to allow consistent generation
 */
public abstract class AbstractCassandraDAO <T extends AbstractCassandraTable> {

    protected final static Logger logger = LoggerFactory.getLogger(AbstractCassandraDAO.class);

    private CassandraDriver db;

    private Class<T> clazz;

    public Request<T> newRequest() {
        return new Request<T>();
    }

    /**
     * Set object class reference for use in Casquatch APIs
     * @return class that was set
     */
    public Class<T> getClazz() {
        return this.clazz;
    }

    /**
     * Set object class reference for use in Casquatch APIs
     * @param clazz class to set
     */
    public void setClazz(Class<T> clazz) {
        this.clazz=clazz;
    }

    /**
     * Get reference to CasandraDriver instance
     * @return db db to set
     */
    public CassandraDriver getDB() {
        return this.db;
    }

    /**
     * Set reference to CasandraDriver instance
     * @param db db to set
     */
    public void setDB(CassandraDriver db) {
        this.db=db;
    }

    /**
     * Wrapper for {@link CassandraDriver#getById CassandraDriver.getById}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/get", method= RequestMethod.POST)
    public Response<T> get(@RequestBody Request<T> request) {
        return new Response<T>(db.getById(this.clazz, request.getPayload()));
    }

    /**
     * Wrapper for {@link CassandraDriver#getOneById CassandraDriver.getOneById}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/get/one", method= RequestMethod.POST)
    public Response<T> getOne(@RequestBody Request<T> request) {
        return new Response<T>(db.getOneById(this.clazz, request.getPayload()));
    }

    /**
     * Wrapper for {@link CassandraDriver#getAllById CassandraDriver.getAllById}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/get/all", method= RequestMethod.POST)
    public Response<T> getAllById(@RequestBody Request<T> request) {
        return new Response<T>(db.getAllById(this.clazz, request.getPayload()));
    }

    /**
     * Wrapper for {@link CassandraDriver#existsById CassandraDriver.existsById}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/exists", method= RequestMethod.POST)
    public Response<Boolean> exists(@RequestBody Request<T> request) {
        return new Response<Boolean>(db.existsById(this.clazz, request.getPayload()));
    }

    /**
     * Wrapper for {@link CassandraDriver#getAllBySolr CassandraDriver.getAllBySolr}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/solr/get/object", method= RequestMethod.POST)
    public Response<T> getAllBySolrObject(@RequestBody Request<T> request) {
        return new Response<T>(db.getAllBySolr(this.clazz, request.getPayload(),request.getOptions().getLimit()));
    }

    /**
     * Wrapper for {@link CassandraDriver#getAllBySolr CassandraDriver.getAllBySolr}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/solr/get/json", method= RequestMethod.POST)
    public Response<T> getAllBySolrJson(@RequestBody Request<String> request) {
        return new Response<T>(db.getAllBySolr(this.clazz, request.getPayload(),request.getOptions().getLimit()));
    }

    /**
     * Wrapper for {@link CassandraDriver#getCountBySolr CassandraDriver.getCountBySolr}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/solr/count", method= RequestMethod.POST)
    public Response<Long> getCountBySolr(@RequestBody Request<T> request) {
        return new Response<Long>(db.getCountBySolr(this.clazz, request.getPayload().toString()));
    }

    /**
     * Wrapper for {@link CassandraDriver#save CassandraDriver.save}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/save", method= RequestMethod.POST)
    public Response<T> save(@RequestBody Request<T> request) {
        db.save(this.clazz, request.getPayload());
        return new Response<T>(Response.Status.SUCCCES);
    }

    /**
     * Wrapper for {@link CassandraDriver#delete CassandraDriver.delete}
     * @param request DAO Request Object
     * @return DAO Response Object
     */
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public Response<T> delete(@RequestBody Request<T> request) {
        db.delete(this.clazz, request.getPayload());
        return new Response<T>(Response.Status.SUCCCES);
    }
}
