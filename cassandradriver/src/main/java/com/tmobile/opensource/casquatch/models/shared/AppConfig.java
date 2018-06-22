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

package com.tmobile.opensource.casquatch.models.shared;

import java.util.Date;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCachable;

/**
 * Generated: Generated class for acms_odp.app_config
 */
@Table(
        name="app_config"
)
public class AppConfig extends AbstractCachable {

    @PartitionKey(0)
    @Column(name="name")
    private String name;

    @Column(name="update_date")
    private Date updateDate;

    @Column(name="value")
    private String value;

    /**
     * Generated: Empty Initializer
     */
    public AppConfig() {}

    /**
     * Generated: Initalize with Partition Keys
     * @param name Partition Key Named name
     */
    public AppConfig(String name) {
        this.name = name;
    }


    /**
     * Generated: Set the primary keys based on provided key
     * @param key dot separated primary key
     */
    @Transient
    public void setCacheKey(String key) {
        String keyVals[];
        if(key.contains(".")) {
            keyVals = key.split(".");
        } else {
            keyVals = new String[] {key};
        }
        this.setName(keyVals[0]);
    }

    /**
     * Generated: Get the cache key which is a dot separated list of primary keys
     * @return key dot separated primary key
     */
    @Transient
    public String getCacheKey() {
        return this.getName();
    }

    /**
     * Generated: Implement getID function from AbstractCassandraTable
     * @return Array of keys
     */
    @Transient
    public Object[] getID() {
        return new Object[]{this.getName()};
    }

    /**
     * Generated: Get procedure for Name
     * @return Value of Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Generated: Set procedure for Name
     * @param name value to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Generated: Get procedure for UpdateDate
     * @return Value of UpdateDate
     */
    public Date getUpdateDate() {
        return this.updateDate;
    }

    /**
     * Generated: Set procedure for UpdateDate
     * @param updateDate value to set
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    /**
     * Generated: Get procedure for Value
     * @return Value of Value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Generated: Set procedure for Value
     * @param value value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
