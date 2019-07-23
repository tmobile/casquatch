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
import lombok.extern.slf4j.Slf4j;

/**
 * This exception class provides a wrapper for the underlying exceptions which then map to custom error codes.
 */

@Slf4j
@Getter
public class DriverException extends RuntimeException {

    public static enum CATEGORIES {
        CASQUATCH_MISSING_GENERATED_CLASS,
        CASQUATCH_MISSING_PARAMETER,
        CASQUATCH_INVALID_CONFIGURATION,
        DATABASE_NO_HOSTS_AVAILABLE,
        DATABASE_AUTHENTICATION,
        DATABASE_CONNECTION,
        APPLICATION_DDL_QUERY,
        APPLICATION_DML_QUERY,
        APPLICATION_INVALID,
        APPLICATION_INTERNAL_DRIVER,
        APPLICATION_UNKNOWN,
        UNHANDLED_DATASTAX,
        UNHANDLED_CASQUATCH
    };

    private CATEGORIES category;
    private String message;
    private Exception exception;

    /**
     * Initializes the Exception Object
     * @param category error code to use
     * @param message error message
     */
    public DriverException(CATEGORIES category , String message) {
        this.category=category;
        this.message = message;
    }

    /**
     * Initializes the Exception Object using another exception object
     * @param exception original exception object
     */
    public DriverException(Exception exception) {
        if (exception instanceof DriverException) {
            this.setException(((DriverException) exception).getException());
        }
        else {
            this.setException(exception);
        }
    }

    /**
     * Set Exception, error, and code based on exception object
     * @param exception original exception object
     */
    private void setException(Exception exception) {
        this.exception = exception;
        //TODO process out more exceptions
        if(exception instanceof com.datastax.oss.driver.api.core.AllNodesFailedException) {
            this.category=CATEGORIES.APPLICATION_DML_QUERY;
            this.message="Query Exception: Query failed on all nodes "+exception.getClass()+": "+this.getException().getMessage();
        }
        else if(exception instanceof com.datastax.oss.driver.api.core.DriverTimeoutException) {
            this.category=CATEGORIES.APPLICATION_DML_QUERY;
            this.message="Query Exception: Query Timed Out "+exception.getClass()+": "+this.getException().getMessage();
        }
        else if(exception instanceof com.datastax.oss.driver.api.core.InvalidKeyspaceException) {
            this.category=CATEGORIES.APPLICATION_DML_QUERY;
            this.message="Query Exception: Invalid Keyspace "+exception.getClass()+": "+this.getException().getMessage();
        }
        else if(exception instanceof com.datastax.oss.driver.api.core.NoNodeAvailableException) {
            this.category=CATEGORIES.APPLICATION_DML_QUERY;
            this.message="Query Exception: No node was available to execute the query "+exception.getClass()+": "+this.getException().getMessage();
        }
        else if(exception instanceof com.datastax.oss.driver.api.core.DriverException) {
            this.category=CATEGORIES.UNHANDLED_DATASTAX;
            this.message="Undefined Datastax Driver Exception: "+exception.getClass()+": "+this.getException().getMessage();
        }
        else {
            this.category=CATEGORIES.UNHANDLED_CASQUATCH;
            this.message="Undefined Exception: "+exception.getClass()+": "+this.getException().getMessage();
        }
        /*if (exception instanceof com.datastax.driver.core.exceptions.NoHostAvailableException) {
            this.setCode(201);
            this.setMessage("No Hosts Available: "+this.getException().getMessage());
        }
        else if (exception instanceof com.datastax.driver.core.exceptions.AuthenticationException) {
            this.setCode(202);
            this.setMessage("Authentication Exception: "+this.getException().getMessage());
        }
        else if (exception instanceof IllegalStateException) {
            this.setCode(203);
            this.setMessage("Connection Exception: "+this.getException().getMessage());
        }
        else if (exception instanceof  com.datastax.driver.core.exceptions.QueryExecutionException) {
            this.setCode(301);
            this.setMessage("Query Execution Exception: "+this.getException().getMessage());
        }
        else if (exception instanceof com.datastax.driver.core.exceptions.InvalidConfigurationInQueryException) {
            this.setCode(302);
            this.setMessage("Invalid Query Configuration Exception: "+this.getException().getMessage());
        }
        else if (exception instanceof com.datastax.driver.core.exceptions.InvalidQueryException) {
            this.setCode(303);
            this.setMessage("Invalid Query Exception: "+this.getException().getMessage());
        }
        else if (exception instanceof com.datastax.driver.core.exceptions.DriverInternalError) {
            this.setCode(304);
            this.setMessage("Internal Driver Exception: "+exception.getCause());
        }
        else {
            this.setCode(399);
            this.setMessage("Unknown exception: "+exception.getClass()+": "+this.getException().getMessage());
        }*/
    }
}
