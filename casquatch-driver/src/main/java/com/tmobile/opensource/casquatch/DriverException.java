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

    /**
     * Enum to hold exception categories
     */
    public enum CATEGORIES {
        CASQUATCH_MISSING_GENERATED_CLASS,
        CASQUATCH_MISSING_PARAMETER,
        CASQUATCH_INVALID_CONFIGURATION,
        DATABASE_NO_HOSTS_AVAILABLE,
        DATABASE_AUTHENTICATION,
        DATABASE_CONNECTION,
        DATABASE_FEATURE_NOT_SUPPORTED,
        APPLICATION_DDL_QUERY,
        APPLICATION_DML_QUERY,
        APPLICATION_INVALID,
        APPLICATION_INTERNAL_DRIVER,
        APPLICATION_UNKNOWN,
        UNHANDLED_DATASTAX,
        UNHANDLED_CASQUATCH
    }

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
        else if(exception instanceof com.datastax.oss.driver.api.core.servererrors.InvalidQueryException) {
            if (exception.getMessage().equals("Undefined column name solr_query")) {
                this.category=CATEGORIES.DATABASE_FEATURE_NOT_SUPPORTED;
                this.message="Query Exception: Solr is not enabled for this table";
            }
            else {
                this.category=CATEGORIES.APPLICATION_DML_QUERY;
                this.message="Query Exception: "+exception.getClass()+": "+this.getException().getMessage();
            }
        }
        else if(exception instanceof com.datastax.oss.driver.api.core.DriverException) {
            this.category=CATEGORIES.UNHANDLED_DATASTAX;
            this.message="Undefined Datastax Driver Exception: "+exception.getClass()+": "+this.getException().getMessage();
        }
        else {
            this.category=CATEGORIES.UNHANDLED_CASQUATCH;
            this.message="Undefined Exception: "+exception.getClass()+": "+this.getException().getMessage();
        }
    }
}
