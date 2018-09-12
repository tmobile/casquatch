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
package com.tmobile.opensource.casquatch.models.junittest;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.String;
import java.lang.Integer;
import java.lang.Double;

/**
 * Generated: Generated UDT class for junittest.junit_udt
 */
@UDT(
    keyspace = "junittest",
    name="junit_udt"
)
public class JunitUdt {

    @Field(name="val1")
    private String val1;
    @Field(name="val2")
    private Integer val2;

    /**
     * Generated: Get procedure for Val1
     * @return Value of Val1
     */
    public String getVal1() {
        return this.val1;
    }

    /**
     * Generated: Set procedure for Val1
     * @param val1 value to set
     */
    public void setVal1(String val1) {
        this.val1 = val1;
    }
    /**
     * Generated: Get procedure for Val2
     * @return Value of Val2
     */
    public Integer getVal2() {
        return this.val2;
    }

    /**
     * Generated: Set procedure for Val2
     * @param val2 value to set
     */
    public void setVal2(Integer val2) {
        this.val2 = val2;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }

}
