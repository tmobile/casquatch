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
 * WITHOUE WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tmobile.opensource.casquatch;

import com.tmobile.opensource.casquatch.annotation.ClusteringColumn;
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
import com.tmobile.opensource.casquatch.tests.podam.CasquatchPodamFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("WeakerAccess")
@Slf4j
public abstract class AbstractEntityTests<E extends AbstractCasquatchEntity>{

    protected static final PodamFactory podamFactory = new CasquatchPodamFactoryImpl();
    public abstract CasquatchDao getCasquatchDao();
    protected Class<E> entityClass;
    protected DatabaseCache<E> databaseCache;
    private Long expiration = 10000L;

    protected QueryOptions queryOptions;

    /**
     * Default constructor based on EntityClass
     * @param entityClass class of entity
     */
    public AbstractEntityTests(Class<E> entityClass) {
        this.entityClass=entityClass;
        this.queryOptions=new QueryOptions();
    }

    /**
     * Getter for Database Cache. Creates if null
     * @return database cache for entity
     */
    protected DatabaseCache<E> getDatabaseCache() {
        if(this.databaseCache==null) {
            this.databaseCache=this.getCasquatchDao().getCache(this.entityClass,expiration);
        }
        return this.databaseCache;
    }

    /**
     * Creates an object and saves it to the database
     * @return created object
     */
    protected E prepObject() {
        E obj = podamFactory.manufacturePojoWithFullData(this.entityClass);
        log.debug("Prep Object Created: {}",obj.toString());
        this.getCasquatchDao().save(this.entityClass, obj);
        assertTrue(this.getCasquatchDao().existsById(this.entityClass, obj));
        return obj;
    }

    /**
     * Create multiple objects and save to the database
     * @param count number of objects to create
     * @return list of created objects
     */
    protected List<E> prepObject(@SuppressWarnings("SameParameterValue") Integer count) {
        log.trace("Prepping {} objects",count);
        List<E> objectList = new ArrayList<>();
        for(int i=0;i<count;i++) {
            objectList.add(prepObject());
        }
        return objectList;
    }

    /**
     * Clean out the created object
     * @param obj object to clean
     */
    protected void cleanObject(E obj) {
        this.getCasquatchDao().delete(this.entityClass, obj);
        assertFalse(this.getCasquatchDao().existsById(this.entityClass, obj));
    }

    /**
     * Clean a list of objects
     * @param objectList list of created objects
     */
    protected void cleanObject(List<E> objectList) {
        for (E t : objectList) {
            cleanObject(t);
        }
    }

    protected void waitForDone(CompletableFuture<?> completableFuture) {
        int maxWait=10;
        for(int i=0;i<=maxWait;i++) {
            if(completableFuture.isDone()) {
                break;
            }
            try {
                Thread.sleep(1000);
            }
            catch(Exception e) {
                return;
            }
        }
    }

    protected void waitForSolrIndex(E obj){
        int maxWait=30;
        List<String> solrQuery = new ArrayList<>();
        try {
            for (Field field : this.entityClass.getDeclaredFields()) {
                if(field.isAnnotationPresent(PartitionKey.class) || field.isAnnotationPresent(ClusteringColumn.class)) {
                    Object fieldValue=this.entityClass.getMethod(CasquatchNamingConvention.javaVariableToJavaGet(field.getName())).invoke(obj);
                    solrQuery.add(String.format("%s:%s", CasquatchNamingConvention.javaVariableToCql(field.getName()), fieldValue));
                }
            }
        }
        catch (Exception e) {
            log.error("Unable to build solr_query",e);
            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_MISSING_GENERATED_CLASS,"Unable to build solr_query");
        }
        log.trace("Using solr_query {}",String.join(" ",solrQuery));
        for(int i=0;i<=maxWait;i++) {
            if(getCasquatchDao().getAllBySolr(this.entityClass,String.join(" ",solrQuery)).size()>0) return;
            try {
                Thread.sleep(1000);
            }
            catch(Exception e) {
                return;
            }
        }
    }

    //As Solr Object parses out some fields, only search for a matching key
    private Boolean containsObjectKey(List<E> objList, E obj) {
        for(E e : objList) {
            if(e.keys().equals(obj.keys())) return true;
        }
        return false;
    }

    private E prepCache() {
        E obj = prepObject();
        E cachedObject;
        cachedObject= getDatabaseCache().get(obj);
        assert(obj.equals(cachedObject));
        return obj;
    }

    private void changeNonKeyField(E obj) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (    !field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class) &&
                    !field.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.CasquatchIgnore.class) &&
                    !field.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.PartitionKey.class) &&
                    !field.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.ClusteringColumn.class)
            ) {
                try {
                    E fakeObject = podamFactory.manufacturePojoWithFullData(entityClass);
                    Method getMethod = entityClass.getDeclaredMethod(CasquatchNamingConvention.javaVariableToJavaGet(field.getName()));
                    Method setMethod = entityClass.getDeclaredMethod(CasquatchNamingConvention.javaVariableToJavaSet(field.getName()),field.getType());
                    setMethod.invoke(obj,getMethod.invoke(fakeObject));
                    return;

                } catch (Exception e) {
                    //Ignore and let it try another
                    log.error("Error changing value",e);
                }
            }
        }
        throw new DriverException(DriverException.CATEGORIES.UNHANDLED_CASQUATCH, "Failed to change field");
    }

    @Test
    public void testDelete() {
        E obj = prepObject();

        this.getCasquatchDao().delete(this.entityClass, obj);
        assertFalse(this.getCasquatchDao().existsById(this.entityClass,obj));
    }

    @Test
    public void testDeleteASync() {
        E obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().deleteAsync(this.entityClass, obj);
        waitForDone(tst);
        assertTrue(tst.isDone());
    }

    @Test
    public void testExistsById() {
        E obj = prepObject();

        assertTrue(this.getCasquatchDao().existsById(this.entityClass,obj));

        cleanObject(obj);
    }

    @Test
    public void testGetAllById() throws IllegalAccessException, InstantiationException {
        List<E> objectList = prepObject(5);

        List<E> testList = this.getCasquatchDao().getAllById(this.entityClass, this.entityClass.newInstance(),10);
        assertTrue(testList.size()>=5);

        cleanObject(objectList);
    }

    @Test
    public void testGetAllByIdLimited() throws IllegalAccessException, InstantiationException {
        List<E> objectList = prepObject(5);

        assertEquals(5, this.getCasquatchDao().getAllById(this.entityClass, this.entityClass.newInstance(), 5).size());

        cleanObject(objectList);
    }

    @Test
    public void testGetById() {
        E obj = prepObject();

        E tstObj = this.getCasquatchDao().getById(this.entityClass,obj);
        assertEquals(obj, tstObj);

        cleanObject(obj);
    }

    @Test
    public void testSave() {
        E obj = prepObject();

        this.getCasquatchDao().save(this.entityClass,obj);

        assertTrue(this.getCasquatchDao().existsById(this.entityClass, obj));

        cleanObject(obj);
    }

    @Test
    public void testSaveASync() {
        E obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().saveAsync(this.entityClass, obj);
        waitForDone(tst);
        assertTrue(tst.isDone());

        cleanObject(obj);
    }

    @Test
    public void testDeleteWithQueryOptions() {
        E obj = prepObject();

        this.getCasquatchDao().delete(this.entityClass, obj,queryOptions);
        assertFalse(this.getCasquatchDao().existsById(this.entityClass,obj));
    }

    @Test
    public void testDeleteASyncWithQueryOptions() {
        E obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().deleteAsync(this.entityClass, obj,queryOptions);
        waitForDone(tst);
        assertTrue(tst.isDone());
    }

    @Test
    public void testExistsByIdWithQueryOptions() {
        E obj = prepObject();

        assertTrue(this.getCasquatchDao().existsById(this.entityClass,obj,queryOptions));

        cleanObject(obj);
    }

    @Test
    public void testGetAllByIdWithQueryOptions() throws IllegalAccessException, InstantiationException {
        List<E> objectList = prepObject(5);

        assertTrue(this.getCasquatchDao().getAllById(this.entityClass, this.entityClass.newInstance(),queryOptions).size()>=5);

        cleanObject(objectList);
    }

    @Test
    public void testGetByIdWithQueryOptions() {
        E obj = prepObject();

        E tstObj = this.getCasquatchDao().getById(this.entityClass,obj, queryOptions);
        assertEquals(obj, tstObj);

        cleanObject(obj);
    }

    @Test
    public void testSaveWithQueryOptions() {
        E obj = prepObject();

        this.getCasquatchDao().save(this.entityClass,obj,queryOptions);

        assertTrue(this.getCasquatchDao().existsById(this.entityClass, obj));

        cleanObject(obj);
    }

    @Test
    public void testSaveWithTTL() {
        E obj = prepObject();

        this.getCasquatchDao().save(this.entityClass,obj, queryOptions.withTTL(100));

        assertTrue(this.getCasquatchDao().existsById(this.entityClass, obj));

        cleanObject(obj);
    }

    @Test
    public void testSaveASyncWithQueryOptions() {
        E obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().saveAsync(this.entityClass, obj, queryOptions);
        waitForDone(tst);
        assertTrue(tst.isDone());

        cleanObject(obj);
    }

    @Test
    public void testGetCache() {
        E obj = prepObject();
        E cachedObject = getDatabaseCache().get(obj);
        log.trace("Created {}",obj);
        log.trace("Returned {}",cachedObject);
        assert(obj.equals(cachedObject));
    }

    @Test
    public void testGetCacheWithChange() {
        E obj = prepCache();
        E cachedObject = getDatabaseCache().get(obj);

        log.trace("Changing Value");
        changeNonKeyField(obj);
        assert(!obj.equals(cachedObject));
        this.getCasquatchDao().save(entityClass,obj);

        log.trace("Get Cached Value");
        cachedObject = getDatabaseCache().get(obj);
        assert(!obj.equals(cachedObject));

        log.trace("Clear Then Get Cached Value");
        getDatabaseCache().clearCache();
        cachedObject = getDatabaseCache().get(obj);
        assert(obj.equals(cachedObject));
    }

    @Test
    public void testGetCacheExpirationWithChange() throws InterruptedException{
        E obj = prepCache();
        E cachedObject = getDatabaseCache().get(obj);

        changeNonKeyField(obj);
        assert(!obj.equals(cachedObject));
        this.getCasquatchDao().save(entityClass,obj);

        log.trace("Changing Value");
        changeNonKeyField(obj);
        log.trace("Changed object: {}",obj);
        assert(!obj.equals(cachedObject));
        this.getCasquatchDao().save(entityClass,obj);

        log.trace("Allow cache to expire");
        Thread.sleep(Math.round(expiration*1.1));

        log.trace("Get Cached Value");
        cachedObject = getDatabaseCache().get(obj);
        assert(obj.equals(cachedObject));
    }

    @Test
    public void testCreateCacheWithDefaultTime() {
        DatabaseCache<E> databaseCache = getCasquatchDao().getCache(this.entityClass);
        assertNotNull(databaseCache);
    }

    @Test
    public void testGetCacheMissing() {
        assertNull(getDatabaseCache().get(podamFactory.manufacturePojoWithFullData(this.entityClass)));
    }

    @Test
    public void testSetCache() {
        E obj = prepObject();

        getDatabaseCache().set((E) obj.keys(),obj);

        assertEquals(getDatabaseCache().get(obj),obj);
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse(prepObject().equals(null));
    }

    @Test
    public void testEqualsWithBadObject() {
        //Testing with unrelated object
        assertFalse(prepObject().equals(new CasquatchDaoBuilder()));
    }

    @Test
    public void testGetAllBySolrQuery() {

        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR)) {
            waitForSolrIndex(obj);
            List<E> tstObj = this.getCasquatchDao().getAllBySolr(this.entityClass, "*:*");
            assertNotNull(tstObj);
            assert(tstObj.size()>0);
            assert(tstObj.contains(obj));
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }

        cleanObject(obj);
    }

    @Test
    public void testGetCountBySolrQuery() {
        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR)) {
            waitForSolrIndex(obj);
            Long count = this.getCasquatchDao().getCountBySolr(this.entityClass,"*:*");
            assert(count > 0);
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }

        cleanObject(obj);
    }

    @Test
    public void testGetAllBySolrQueryWithOptions() {
        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR)) {
            waitForSolrIndex(obj);
            List<E> tstObj = this.getCasquatchDao().getAllBySolr(this.entityClass,"*:*",queryOptions.withConsistencyLevel("LOCAL_ONE").withAllColumns());
            assertNotNull(tstObj);
            assert(tstObj.size()>0);
            assert(tstObj.contains(obj));
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }
        cleanObject(obj);
    }


    @Test
    public void testGetCountBySolrQueryWithOptions() {
        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR)) {
            waitForSolrIndex(obj);
            Long count = this.getCasquatchDao().getCountBySolr(this.entityClass,"*:*",queryOptions.withConsistencyLevel("LOCAL_ONE").withAllColumns());
            assert(count > 0);
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }

        cleanObject(obj);
    }

    @Test
    public void testGetAllBySolrObject() {

        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR_OBJECT)) {
            waitForSolrIndex(obj);
            List<E> tstObj = this.getCasquatchDao().getAllBySolr(this.entityClass, obj);
            assertNotNull(tstObj);
            assert(tstObj.size()>0);
            assert(containsObjectKey(tstObj,obj));
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }

        cleanObject(obj);
    }

    @Test
    public void testGetCountBySolrObject() {
        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR_OBJECT)) {
            waitForSolrIndex(obj);
            Long count = this.getCasquatchDao().getCountBySolr(this.entityClass,obj);
            assertEquals(1, (long) count);
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }

        cleanObject(obj);
    }

    @Test
    public void testGetAllBySolrObjectWithOptions() {
        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR_OBJECT)) {
            waitForSolrIndex(obj);
            List<E> tstObj = this.getCasquatchDao().getAllBySolr(this.entityClass,obj,queryOptions.withConsistencyLevel("LOCAL_ONE").withAllColumns());
            assertNotNull(tstObj);
            assert(tstObj.size()>0);
            assert(containsObjectKey(tstObj,obj));
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }
        cleanObject(obj);
    }


    @Test
    public void testGetCountBySolrObjectWithOptions() {
        E obj = prepObject();

        if(getCasquatchDao().checkFeature(CasquatchDao.FEATURES.SOLR_OBJECT)) {
            waitForSolrIndex(obj);
            Long count = this.getCasquatchDao().getCountBySolr(this.entityClass,obj,queryOptions.withConsistencyLevel("LOCAL_ONE").withAllColumns());
            assertEquals(1, (long) count);
        }
        else {
            assertThrows(DriverException.class, () -> this.getCasquatchDao().getAllBySolr(this.entityClass, obj));
        }

        cleanObject(obj);
    }
}
