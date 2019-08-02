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

package com.tmobile.opensource.casquatch.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Response wrapper for use with Rest API
 * @param <T> Entity Type to Wrap
 */
@Slf4j
@Getter
public class Response<T> {
    /**
     *  Response Status
     */
    public enum Status {
        /**
         * Success - Payload found and included
         */
        SUCCESS,
        /**
         * No Data Found - Payload contains no results
         */
        NO_DATA_FOUND,
        /**
         * Error - Failed to load data
         */
        ERROR
    }

    Status status = Status.NO_DATA_FOUND;

    private String payloadClass;
    private List<T> payload;
    private Date timestamp = new Date();

    /**
     * No argument constructor for Response
     */
    public Response() {

    }

    /**
     * Construct Response object no payload and specified status.
     * @param status status
     */
    public Response(Status status) {
        this.status=status;
    }

    /**
     * Construct Response object with provided payload. Determine status based on if payload has data
     * @param payload payload object
     */
    public Response(T payload) {
        this.addPayload(payload);
    }

    /**
     * Construct Response object with provided payload (List). Determine status based on if payload has data
     * @param payload payload list
     */
    public Response(List<T> payload) {
        this.addPayload(payload);
    }

    /**
     * Construct Response object with provided payload and specified status.
     * @param payload payload object
     * @param status status
     */
    public Response(T payload, Status status) {
        this(Collections.singletonList(payload),status);
    }

    /**
     * Construct Response object with provided payload (List) and specified status
     * @param payload payload list
     * @param status status
     */
    public Response(List<T> payload, Status status) {
        this.addPayload(payload);
        this.status=status;
    }

    /**
     * Helper function to add a payload item and set class and status if not yet defined
     * @param newPayload payload item
     */
    private void addPayload(T newPayload) {
        if(newPayload!=null) {
            if (this.payload == null) {
                this.payload = new ArrayList<>();
                this.payloadClass=payload.getClass().toString();
                this.status=Status.SUCCESS;
            }
            this.payload.add(newPayload);
        }
    }

    /**
     * Helper function to add a list of payload items
     * @param newPayload list of payload items
     */
    private void addPayload(List<T> newPayload) {
        if(newPayload != null && newPayload.size()>0 && newPayload.get(0) != null) {
            for(T entry : newPayload) {
                addPayload(entry);
            }
        }
    }

    /**
     * Return object as json string
     */
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }

}
