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

public class Request<T> {

    class Options {

        String consistency; //TODO

        Integer limit;

        public Options() {}

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

    }

    private String payloadClass;

    private Options options = new Options();

    private T payload;

    /**
     * No arg constructor for Request
     */
    public Request() {

    }

    /**
     * Constructor with payload
     */
    public Request(T payload) {
        this.setPayload(payload);
    }

    /**
     * Get Payload
     * @return payload
     */
    public T getPayload() {
        return payload;
    }

    /**
     * Set Payload
     * @param payload payload to set
     */
    public void setPayload(T payload) {
        this.payload = payload;
        this.setPayloadClass(payload.getClass().toString());
    }

    /**
     * Get Options object
     * @return options object
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Set Options object
     * @param options options to set
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * Get Payload Class as string
     * @return payload class
     */
    public String getPayloadClass() {
        return payloadClass;
    }

    /**
     * Set string name of payload class
     * @param payloadClass payload class name
     */
    public void setPayloadClass(String payloadClass) {
        this.payloadClass = payloadClass;
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
