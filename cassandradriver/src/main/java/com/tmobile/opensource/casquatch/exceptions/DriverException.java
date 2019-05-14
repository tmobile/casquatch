/* Copyright 2018 T-Mobile US, Inc.
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
package com.tmobile.opensource.casquatch.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmobile.opensource.casquatch.CassandraDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * This exception class provides a wrapper for the underlying exceptions which then map to custom error codes.
 *
 * Error Codes
 * <ul>
 *   <li>2XX - Database Exceptions
 *      <ul>
 *          <li>201 - No Hosts Available</li>
 *          <li>202 - Authentication Error</li>
 *          <li>203 - Connection Exception</li>
 *      </ul>
 *   </li>
 *   <li>
 *      3XX - Application Exceptions
 *      <ul>
 *          <li>301 - DML Query Exception</li>
 *          <li>302 - DDL Query Exception</li>
 *          <li>303 - Invalid Query Exception</li>
 *          <li>304 - Internal Driver Exception</li>
 *          <li>399 - Unknown Exception</li>
*       </ul>
 *   </li>
 *   <li>
 *      4XX - Driver Exceptions
 *      <ul>
 *          <li>401 - Unconfigured Feature</li>
 *          <li>402 - Unsupported Feature</li>
*       </ul>
 *   </li>
 * </ul>
 * @version 1.0
 * @since 2018-03-13
 */
public class DriverException extends RuntimeException {

    int code;
    String message;
    Exception exception;

    private final static Logger logger = LoggerFactory.getLogger(CassandraDriver.class);

    /**
     * Initializes the Exception Object
     * @param code error code to use
     * @param message error message
     */
    public DriverException(int code,String message) {
        this.setCode(code);
        this.setMessage(message);
        logger.error("DriverException: ["+this.getCode()+"] "+this.getMessage());
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
            logger.error("DriverException: ["+this.getCode()+"] "+this.getMessage(),exception);
        }
    }

    /**
     * Get Error Code
     * @return error code
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Set Error Code
     * @param code error code number to use
     */
    private void setCode(int code) {
        this.code = code;
    }

    /**
     * Get Error Message
     * @return error message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set Error Message
     * @param message error message to use
     */
    private void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get Original Exception. Allows deeper handling of the underlying driver
     * @return Raw exception
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * Set Exception, error, and code based on exception object
     * @param exception original exception object
     */
    private void setException(Exception exception) {
        this.exception = exception;
        if (exception instanceof com.datastax.driver.core.exceptions.NoHostAvailableException) {
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
        }
    }
}
