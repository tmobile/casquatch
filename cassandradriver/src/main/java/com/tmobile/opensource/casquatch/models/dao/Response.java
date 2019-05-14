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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Response<T> {
    /**
     *  Response Status
     */
    public enum Status {
        /**
         * Success - Payload found and included
         */
        SUCCCES,
        /**
         * No Data Found - Payload contains no results
         */
        NO_DATA_FOUND,
        /**
         * Error - Failed to load data
         */
        ERROR
    }

    Status status;

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
        this.setStatus(status);
    }

    /**
     * Construct Response object with provided payload. Determine status based on if payload has data
     * @param payload payload object
     */
    public Response(T payload) {
        this(Arrays.asList(payload));
    }

    /**
     * Construct Response object with provided payload (List). Determine status based on if payload has data
     * @param payload payload list
     */
    public Response(List<T> payload) {
        if(payload != null && payload.size()>0 && payload.get(0) != null) {
            this.setPayload(payload);
            try {
                this.setPayloadClass(payload.get(0).getClass().toString());
            }
            catch (Exception e) {

            }
            this.setStatus(Status.SUCCCES);
        }
        else {
            this.setStatus(Status.NO_DATA_FOUND);
        }
    }

    /**
     * Construct Response object with provided payload and specified status.
     * @param payload payload object
     * @param status status
     */
    public Response(T payload, Status status) {
        this(new ArrayList<T>(Arrays.asList(payload)),status);
    }

    /**
     * Construct Response object with provided payload (List) and specified status
     * @param payload payload list
     * @param status status
     */
    public Response(List<T> payload, Status status) {
        this.setPayload(payload);
        this.setPayloadClass(payload.get(0).getClass().toString());
        this.setStatus(status);
    }

    /**
     * Get Payload
     * @return payload
     */
    public List<T> getPayload() {
        return payload;
    }

    /**
     * Set Payload
     * @param payload payload
     */
    public void setPayload(List<T> payload) {
        this.payload = payload;
    }

    /**
     * Get Payload Class as string
     * @return payload class
     */
    public String getPayloadClass() {
        return payloadClass;
    }

    /**
     * Set Payload Class as string
     * @param payloadClass payload class
     */
    public void setPayloadClass(String payloadClass) {
        this.payloadClass = payloadClass;
    }

    /**
     * Get Response Status
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set Response Status
     * @param status status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Get Response Timestamp
     * @return timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set Response Timestamp
     * @param timestamp timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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
