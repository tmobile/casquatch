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
import com.tmobile.opensource.casquatch.QueryOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request wrapper for use with RestAPI
 * @param <T> entity type to wrap
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor
@Getter @Setter
public class Request<T> {

    private String payloadClass;

    private QueryOptions queryOptions = new QueryOptions();

    private T payload;

    /**
     * Constructor with payload
     * @param payload request payload
     */
    public Request(T payload) {
        this.setPayload(payload);
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
     * Return object as json string
     * @return json representation of object
     */
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }
}
