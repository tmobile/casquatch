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

import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.CasquatchDao;
import com.tmobile.opensource.casquatch.tests.podam.CasquatchPodamFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Objects;

/**
 * Load functionality wrapped around a generic entity
 * @param <E> entity to wrap
 */
@Slf4j
class LoadWrapper<E extends AbstractCasquatchEntity> {

    private final Class<E> clazz;
    private final CasquatchDao db;
    private static final PodamFactory podamFactory = new CasquatchPodamFactoryImpl();

    private Integer totalWrites=0;
    private Long totalDBWriteTime = 0L;
    private Long totalWriteTime = 0L;

    private Integer totalReads=0;
    private Long totalDBReadTime = 0L;
    private Long totalReadTime = 0L;

    private Integer totalChecks=0;
    private Integer successChecks=0;
    private Integer failChecks=0;

    private Integer errorCount = 0;

    /**
     * Simple constructor
     * @param clazz class reference
     * @param db db reference
     */
    public LoadWrapper(Class<E> clazz, CasquatchDao db) {
        this.clazz=clazz;
        this.db=db;
    }

    /**
     * Create a random object
     * @return random object
     */
    private E generate() {
        return podamFactory.manufacturePojoWithFullData(this.clazz);
    }

    /**
     * Write the object to the db
     * @param obj object to write
     * @return written object;
     */
    private E write(E obj) {

        StopWatch sw = new StopWatch();
        StopWatch dbsw = new StopWatch();
        sw.start();

        try {
            log.trace("Writing object with id "+obj.keys().toString()+" : "+obj.toString());
            dbsw.start();
            db.save(this.clazz, obj);
            dbsw.stop();
            log.trace("DB Write performed in "+timeFormat(dbsw.getNanoTime())+".");
            log.trace("Successfully wrote object: "+obj.toString());
        }
        catch (Exception e) {
            log.error("Failed to write object",e);
            errorCount++;
        }
        sw.stop();
        totalWrites++;
        totalDBWriteTime=totalDBWriteTime+dbsw.getNanoTime();
        totalWriteTime=totalWriteTime+sw.getNanoTime();
        log.trace("Write performed in "+sw.getNanoTime()+"ms");
        log.debug("Write Complete. ID: "+obj.keys().toString()+". DB Time: "+timeFormat(dbsw.getNanoTime())+". Total Time:"+timeFormat(sw.getNanoTime())+".");
        return obj;
    }

    /**
     * Read the provided object by keys
     * @param obj object to query with
     * @return result of query
     */
    private E read(E obj) {
        StopWatch sw = new StopWatch();
        StopWatch dbsw = new StopWatch();
        sw.start();
        log.trace("Reading object with id: "+obj.keys().toString());
        E readObj = null;
        try {
            dbsw.start();
            readObj = db.getById(this.clazz, obj);
            dbsw.stop();
            log.trace("DB Write performed in "+timeFormat(dbsw.getNanoTime())+".");
            log.trace("Read object: "+readObj.toString());
        }
        catch (Exception e) {
            log.error("Failed to read object",e);
            errorCount++;
        }
        sw.stop();
        totalReads++;
        totalDBReadTime=totalDBReadTime+dbsw.getNanoTime();
        totalReadTime=totalReadTime+sw.getNanoTime();
        log.trace("Read performed in "+timeFormat(sw.getNanoTime())+".");
        log.debug("Read Complete. ID: "+obj.keys().toString()+". DB Time: "+timeFormat(dbsw.getNanoTime())+". Total Time:"+timeFormat(sw.getNanoTime())+".");
        return readObj;
    }

    /**
     * Compare two objects
     * @param obj1 object 1
     * @param obj2 object 2
     */
    private void check(E obj1, E obj2) {
        if(obj1==null && obj2==null) {
            log.error("Must read and write in same transaction to check");
        }
        else {
            totalChecks++;
            if(Objects.requireNonNull(obj1).equals(obj2)) {
                log.debug("Consistency Check: Passed");
                successChecks++;
            }
            else {
                log.debug("Consistency Check: Failed");
                failChecks++;
            }
        }
    }

    /**
     * Helper function to convert time to ms
     * @param input time in long
     * @return time in ms
     */
    private String timeFormat(Long input) {
        double tmp = Math.round(input*100/1000000)/100.00;
        return tmp +"ms";
    }

    /**
     * Attempt to sleep for a given ms
     * @param delay ms to sleep
     */
    private void delay(Integer delay) {
        if(delay > 0) {
            log.trace("Sleeping for "+delay+"ms");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.error("Sleep interupted",e);
            }
        }
    }

    /**
     * Run the load test for this object
     * @param loadTestConfig load test configuration object
     */
    public void run(LoadTestConfig loadTestConfig) {
        E warmup = generate();
        db.save(this.clazz, warmup);
        db.getById(this.clazz, warmup);

        log.info("Run Starting. Loops: "+loadTestConfig.getLoops()+". Delay: "+loadTestConfig.getDelay());
        for (int x=0;x<loadTestConfig.getLoops();x++) {

            E obj = generate();
            E readObj = null;
            E writeObj = null;

            if(loadTestConfig.getDoWrite()) writeObj = write(obj);

            if(loadTestConfig.getDelay() > 0) delay(loadTestConfig.getDelay());

            if(loadTestConfig.getDoRead()) readObj=read(obj);

            if(loadTestConfig.getDelay() > 0) delay(loadTestConfig.getDelay());

            if(loadTestConfig.getDoRead()) read(obj);

            if(loadTestConfig.getDoCheck()) check(writeObj,readObj);

            if(loadTestConfig.getDelay() > 0) delay(loadTestConfig.getDelay());
        }
        log.info("Run complete with "+loadTestConfig.getLoops()+" loops");
        if(loadTestConfig.getDoWrite()) {
            log.info("Writes: "+totalReads+". Average DB Time:"+timeFormat(totalDBWriteTime/totalWrites)+". Average Total Time: "+timeFormat(totalWriteTime/totalWrites)+".");
        }
        if(loadTestConfig.getDoRead()) {
            log.info("Reads: "+totalReads+". Average DB Time:"+timeFormat(totalDBReadTime/totalReads)+". Average Total Time: "+timeFormat(totalReadTime/totalReads)+".");
        }
        if(loadTestConfig.getDoCheck()) {
            log.info("Consistency Checks: "+totalChecks+". Success: "+successChecks+". Failures: "+failChecks+".");
        }
    }

}
