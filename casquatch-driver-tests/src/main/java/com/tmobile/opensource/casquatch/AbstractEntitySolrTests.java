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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Abstract Class to be extended for Entity Solr Tests
 * @param <E> Entity Class that extends AbstractCasquatchEntity
 */
public abstract class AbstractEntitySolrTests<E extends AbstractCasquatchEntity> extends AbstractEntityTests<E> {

    public AbstractEntitySolrTests(Class<E> entityClass) {
        super(entityClass);
    }

    @Test
    public void testGetAllBySolrObject() {
        E obj = prepObject();

        List<E> tstObj = this.getCasquatchDao().getAllBySolr(this.entityClass,obj);
        assertEquals(1, tstObj.size());
        assertEquals(obj, tstObj.get(0));

        cleanObject(obj);
    }


    @Test
    public void testGetCountBySolrObject() {
        E obj = prepObject();

        Long count = this.getCasquatchDao().getCountBySolr(this.entityClass,obj);
        assertEquals(1, (long) count);

        cleanObject(obj);
    }

    @Test
    public void testGetAllBySolrObjectWithOptions() {
        E obj = prepObject();

        List<E> tstObj = this.getCasquatchDao().getAllBySolr(this.entityClass,obj,queryOptions);
        assertEquals(1, tstObj.size());
        assertEquals(obj, tstObj.get(0));

        cleanObject(obj);
    }


    @Test
    public void testGetCountBySolrObjectWithOptions() {
        E obj = prepObject();

        Long count = this.getCasquatchDao().getCountBySolr(this.entityClass,obj,queryOptions);
        assertEquals(1, (long) count);

        cleanObject(obj);
    }

}
