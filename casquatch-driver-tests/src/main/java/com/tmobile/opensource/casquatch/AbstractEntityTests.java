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

import com.tmobile.opensource.casquatch.tests.podam.CasquatchPodamFactoryImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

@Slf4j
@Getter @Setter
public abstract class AbstractEntityTests<T extends AbstractCasquatchEntity>{

    protected static final PodamFactory podamFactory = new CasquatchPodamFactoryImpl();
    public abstract CasquatchDao getCasquatchDao();
    protected Class<T> tableClass;
    protected DatabaseCache<T> databaseCache;
    private Long expiration = new Long(100);

    protected QueryOptions queryOptions;

    public AbstractEntityTests(Class<T> tableClass) {
        this.setTableClass(tableClass);
        this.queryOptions=new QueryOptions();
        databaseCache = this.getCasquatchDao().getCache(this.getTableClass(),expiration);
    }

    /**
     * Creates an object and saves it to the database
     * @return created object
     */
    protected T prepObject() {
        T obj = podamFactory.manufacturePojoWithFullData(this.getTableClass());
        log.debug("Prep Object Created: {}",obj.toString());
        this.getCasquatchDao().save(this.getTableClass(), obj);
        assertTrue(this.getCasquatchDao().existsById(this.getTableClass(), obj));
        return obj;
    }

    /**
     * Create multiple objects and save to the database
     * @param count number of objects to create
     * @return list of created objects
     */
    protected List<T> prepObject(Integer count) {
        log.trace("Prepping {} objects",count);
        List<T> objectList = new ArrayList<>();
        for(int i=0;i<count;i++) {
            objectList.add(prepObject());
        }
        return objectList;
    }

    /**
     * Clean out the created object
     * @param obj object to clean
     */
    protected void cleanObject(T obj) {
        this.getCasquatchDao().delete(this.getTableClass(), obj);
        assertFalse(this.getCasquatchDao().existsById(this.getTableClass(), obj));
    }

    /**
     * Clean a list of objects
     * @param objectList list of created objects
     */
    protected void cleanObject(List<T> objectList) {
        for (T t : objectList) {
            cleanObject(t);
        }
    }

    protected void waitForDone(CompletableFuture<?> completableFuture) {
        Integer maxWait=10;
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



    private T prepCache() {
        T obj = prepObject();
        T cachedObject;
        cachedObject= databaseCache.get(obj);
        assert(obj.equals(cachedObject));
        return obj;
    }

    private void changeNonKeyField(T obj) {
        for (Field field : tableClass.getDeclaredFields()) {
            if (    !field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class) &&
                    !field.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.CasquatchIgnore.class) &&
                    !field.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.PartitionKey.class) &&
                    !field.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.ClusteringColumn.class)
            ) {
                try {
                    T fakeObject = podamFactory.manufacturePojoWithFullData(tableClass);
                    Method getMethod = tableClass.getDeclaredMethod(CasquatchNamingConvention.javaVariableToJavaGet(field.getName()));
                    Method setMethod = tableClass.getDeclaredMethod(CasquatchNamingConvention.javaVariableToJavaSet(field.getName()),field.getType());
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
        T obj = prepObject();

        this.getCasquatchDao().delete(this.getTableClass(), obj);
        assertFalse(this.getCasquatchDao().existsById(this.getTableClass(),obj));
    }

    @Test
    public void testDeleteASync() {
        T obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().deleteAsync(this.getTableClass(), obj);
        waitForDone(tst);
        assertTrue(tst.isDone());
    }

    @Test
    public void testExistsById() {
        T obj = prepObject();

        assertTrue(this.getCasquatchDao().existsById(this.getTableClass(),obj));

        cleanObject(obj);
    }

    @Test
    public void testGetAllById() throws IllegalAccessException, InstantiationException {
        List<T> objectList = prepObject(5);

        List<T> testList = this.getCasquatchDao().getAllById(this.getTableClass(), this.getTableClass().newInstance(),10);
        assertTrue(testList.size()>=5);

        cleanObject(objectList);
    }

    @Test
    public void testGetAllByIdLimited() throws IllegalAccessException, InstantiationException {
        List<T> objectList = prepObject(5);

        assertEquals(5, this.getCasquatchDao().getAllById(this.getTableClass(), this.getTableClass().newInstance(), 5).size());

        cleanObject(objectList);
    }

    @Test
    public void testGetById() {
        T obj = prepObject();

        T tstObj = this.getCasquatchDao().getById(this.getTableClass(),obj);
        assertEquals(obj, tstObj);

        cleanObject(obj);
    }

    @Test
    public void testSave() {
        T obj = prepObject();

        assertTrue(this.getCasquatchDao().existsById(this.getTableClass(), obj));

        cleanObject(obj);
    }

    @Test
    public void testSaveASync() {
        T obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().saveAsync(this.getTableClass(), obj);
        waitForDone(tst);
        assertTrue(tst.isDone());

        cleanObject(obj);
    }

    @Test
    public void testDeleteWithQueryOptions() {
        T obj = prepObject();

        this.getCasquatchDao().delete(this.getTableClass(), obj,queryOptions);
        assertFalse(this.getCasquatchDao().existsById(this.getTableClass(),obj));
    }

    @Test
    public void testDeleteASyncWithQueryOptions() {
        T obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().deleteAsync(this.getTableClass(), obj,queryOptions);
        waitForDone(tst);
        assertTrue(tst.isDone());
    }

    @Test
    public void testExistsByIdWithQueryOptions() {
        T obj = prepObject();

        assertTrue(this.getCasquatchDao().existsById(this.getTableClass(),obj,queryOptions));

        cleanObject(obj);
    }

    @Test
    public void testGetAllByIdWithQueryOptions() throws IllegalAccessException, InstantiationException {
        List<T> objectList = prepObject(5);

        assertTrue(this.getCasquatchDao().getAllById(this.getTableClass(), this.getTableClass().newInstance(),queryOptions).size()>=5);

        cleanObject(objectList);
    }

    @Test
    public void testGetByIdWithQueryOptions() {
        T obj = prepObject();

        T tstObj = this.getCasquatchDao().getById(this.getTableClass(),obj, queryOptions);
        assertEquals(obj, tstObj);

        cleanObject(obj);
    }

    @Test
    public void testSaveWithQueryOptions() {
        T obj = prepObject();

        assertTrue(this.getCasquatchDao().existsById(this.getTableClass(), obj, queryOptions));

        cleanObject(obj);
    }

    @Test
    public void testSaveASyncWithQueryOptions() {
        T obj = prepObject();

        CompletableFuture<Void> tst = this.getCasquatchDao().saveAsync(this.getTableClass(), obj, queryOptions);
        waitForDone(tst);
        assertTrue(tst.isDone());

        cleanObject(obj);
    }



    @Test
    public void testGetCache() {
        T obj = prepObject();
        T cachedObject = databaseCache.get(obj);
        assert(obj.equals(cachedObject));
    }

    @Test
    public void testGetCacheWithChange() {
        T obj = prepCache();
        T cachedObject = databaseCache.get(obj);

        log.trace("Changing Value");
        changeNonKeyField(obj);
        assert(!obj.equals(cachedObject));
        this.getCasquatchDao().save(tableClass,obj);

        log.trace("Get Cached Value");
        cachedObject = databaseCache.get(obj);
        assert(!obj.equals(cachedObject));

        log.trace("Clear Then Get Cached Value");
        databaseCache.clearCache();
        cachedObject = databaseCache.get(obj);
        assert(obj.equals(cachedObject));
    }

    @Test
    public void testGetCacheExpirationWithChange() throws InterruptedException{
        T obj = prepCache();
        T cachedObject = databaseCache.get(obj);

        changeNonKeyField(obj);
        assert(!obj.equals(cachedObject));
        this.getCasquatchDao().save(tableClass,obj);

        log.trace("Changing Value");
        changeNonKeyField(obj);
        log.trace("Changed object: {}",obj);
        assert(!obj.equals(cachedObject));
        this.getCasquatchDao().save(tableClass,obj);

        log.trace("Allow cache to expire");
        Thread.sleep(expiration);

        log.trace("Get Cached Value");
        cachedObject = databaseCache.get(obj);
        assert(obj.equals(cachedObject));
    }
}
